package com.gordan.luckydraw.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.gordan.luckydraw.enums.CustomError;
import com.gordan.luckydraw.exception.AppException;
import com.gordan.luckydraw.model.Activity;
import com.gordan.luckydraw.model.Prize;
import com.gordan.luckydraw.repository.ActivityRepository;
import com.gordan.luckydraw.repository.PrizeRepository;

@Service
public class LotteryService {
    private static final String NO_PRIZE_LABEL = "銘謝惠顧";
    private static final String USER_DRAW_LOCK_KEY_TEMPLATE = "DRAW_LOCK_%d_%s";
    private static final String USER_DRAW_COUNT_KEY_TEMPLATE = "ACT_%d_USER_DRAW_COUNT";
    private static final String PRIZE_STOCK_KEY_TEMPLATE = "ACT_%d_PRIZE_STOCK";
    @Autowired
    private PrizeRepository prizeRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void setPrizes(Long activityId, List<Prize> prizes) {
        if (!activityRepository.findById(activityId).isPresent()) {
            throw new AppException(CustomError.ACTIVITY_NOT_FOUND);
        }

        double probability = prizeRepository.findByActivityId(activityId).stream()
                .mapToDouble(Prize::getProbability)
                .sum();
        probability += prizes.stream()
                .mapToDouble(Prize::getProbability)
                .sum();
        if (probability > 1.0) {
            throw new AppException(CustomError.PROBABILITY_EXCEED);
        }

        prizeRepository.saveAll(
                prizes.stream().map(prize -> {
                    prize.setActivityId(activityId);
                    return prize;
                }).toList());
    }

    public List<String> draw(Long activityId, String userId, int times) {
        // 實現抽獎邏輯
        String userDrawLockKey = String.format(USER_DRAW_LOCK_KEY_TEMPLATE, activityId, userId);
        if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(userDrawLockKey, "LOCK", Duration.ofSeconds(times)))) {
            try {
                // 檢查用戶是否達到最大抽獎次數
                Activity activity = activityRepository.findById(activityId)
                        .orElseThrow(() -> new AppException(CustomError.ACTIVITY_NOT_FOUND));
                int maxDraws = activity.getMaxNumberOfDrawsPerUser();

                // 檢查用戶當前抽獎次數
                String userDrawCountKey = String.format(USER_DRAW_COUNT_KEY_TEMPLATE, activityId);
                Integer currentDraws = (Integer) redisTemplate.opsForHash().get(userDrawCountKey, userId);
                if (currentDraws == null) {
                    currentDraws = 0;
                }

                if (currentDraws + times > maxDraws) {
                    throw new AppException(CustomError.MAX_DRAWS_EXCEEDED);
                }

                List<Prize> prizes = prizeRepository.findByActivityId(activityId);
                if (prizes.isEmpty()) {
                    throw new AppException(CustomError.PRIZE_NOT_FOUND);
                }

                // 取得獎品庫存，過濾掉庫存為0的獎品
                String activityPrizeStockKey = String.format(PRIZE_STOCK_KEY_TEMPLATE, activityId);
                List<Prize> availablePrizes = new ArrayList<>();
                for (Prize prize : prizes) {
                    Object stockObj = redisTemplate.opsForHash().get(activityPrizeStockKey, prize.getId());
                    long stock = stockObj == null ? 0 : Long.parseLong(stockObj.toString());
                    if (stock > 0) {
                        availablePrizes.add(prize);
                    }
                }

                List<String> results = new ArrayList<>();
                if (availablePrizes.isEmpty()) {
                    // 全部獎品都沒庫存，直接回傳 NO_PRIZE_LABEL
                    for (int i = 0; i < times; i++) {
                        results.add(NO_PRIZE_LABEL);
                    }
                    redisTemplate.opsForHash().put(userDrawCountKey, userId, currentDraws + times);
                    return results;
                }

                // Precompute cumulative probabilities for available prizes
                List<Double> cumulativeProbabilities = new ArrayList<>();
                double cumulative = 0;
                for (Prize prize : availablePrizes) {
                    cumulative += prize.getProbability();
                    cumulativeProbabilities.add(cumulative);
                }

                Random random = new Random();
                for (int i = 0; i < times; i++) {
                    double rand = random.nextDouble() * cumulative;
                    String result = NO_PRIZE_LABEL;
                    for (int j = 0; j < availablePrizes.size(); j++) {
                        Prize prize = availablePrizes.get(j);
                        if (rand <= cumulativeProbabilities.get(j)) {
                            Long stock = redisTemplate.opsForHash().increment(activityPrizeStockKey, prize.getId(), -1);
                            if (stock < 0) {
                                redisTemplate.opsForHash().increment(activityPrizeStockKey, prize.getId(), 1);
                                break;
                            }
                            result = prize.getName();
                            break;
                        }
                    }
                    results.add(result);
                }
                redisTemplate.opsForHash().put(userDrawCountKey, userId, currentDraws + times);
                return results;
            } finally {
                redisTemplate.delete(userDrawLockKey);
            }
        } else {
            throw new AppException(CustomError.USER_IS_DRAWING);
        }
    }

}
