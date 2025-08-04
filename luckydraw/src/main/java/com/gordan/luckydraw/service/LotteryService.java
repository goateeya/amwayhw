package com.gordan.luckydraw.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.gordan.luckydraw.enums.CustomError;
import com.gordan.luckydraw.exception.AppException;
import com.gordan.luckydraw.exception.PrizeOutOfStockException;
import com.gordan.luckydraw.model.Activity;
import com.gordan.luckydraw.model.DrawLog;
import com.gordan.luckydraw.model.Prize;
import com.gordan.luckydraw.repository.ActivityRepository;
import com.gordan.luckydraw.repository.DrawLogRepository;
import com.gordan.luckydraw.repository.PrizeRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class LotteryService {
    private static final String NO_PRIZE_LABEL = "銘謝惠顧";
    private static final String OUT_OF_STOCK_LABEL = "獎品已抽完, 請稍後再試";
    private static final String USER_DRAW_LOCK_KEY_TEMPLATE = "DRAW_LOCK_%d_%s";
    private static final String USER_DRAW_COUNT_KEY_TEMPLATE = "ACT_%d_USER_DRAW_COUNT";
    private static final String PRIZE_STOCK_KEY_TEMPLATE = "ACT_%d_PRIZE_STOCK";
    @Autowired
    private PrizeRepository prizeRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private DrawLogRepository drawLogRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<Prize> savePrizes(Long activityId, List<Prize> prizes) {
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

        return prizeRepository.saveAll(
                prizes.stream().map(prize -> {
                    prize.setActivityId(activityId);
                    return prize;
                }).toList());
    }

    public Activity createActivity(com.gordan.luckydraw.model.Activity activity) {
        return activityRepository.save(activity);
    }

    public List<String> draw(Long activityId, String userId, int times) {
        // 實現抽獎邏輯
        String userDrawLockKey = String.format(USER_DRAW_LOCK_KEY_TEMPLATE, activityId, userId);
        if (Boolean.TRUE
                .equals(redisTemplate.opsForValue().setIfAbsent(userDrawLockKey, "LOCK", Duration.ofSeconds(times)))) {
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

                // 取得獎品庫存，null 表示未初始化，需從 DB 撈出庫存並 set cache
                String activityPrizeStockKey = String.format(PRIZE_STOCK_KEY_TEMPLATE, activityId);
                List<Prize> availablePrizes = new ArrayList<>();
                for (Prize prize : prizes) {
                    Object stockObj = redisTemplate.opsForHash().get(activityPrizeStockKey, prize.getId().toString());
                    long stock;
                    if (stockObj == null) {
                        // 未初始化，從 DB 撈出庫存
                        stock = prize.getStock();
                        redisTemplate.opsForHash().put(activityPrizeStockKey, prize.getId().toString(), stock);
                    } else {
                        stock = Long.parseLong(stockObj.toString());
                    }
                    if (stock > 0) {
                        availablePrizes.add(prize);
                    }
                }

                if (availablePrizes.isEmpty()) {
                    return List.of(OUT_OF_STOCK_LABEL);
                }

                // Precompute cumulative probabilities for available prizes
                List<Double> cumulativeProbabilities = new ArrayList<>();
                double cumulative = 0;
                for (Prize prize : availablePrizes) {
                    cumulative += prize.getProbability();
                    cumulativeProbabilities.add(cumulative);
                }

                Random random = new Random();

                int actualDrawTimes = 0;
                List<DrawLog> drawLogs = new ArrayList<>();
                try {
                    for (int i = 0; i < times; i++) {
                        double rand = random.nextDouble() * cumulative;
        
                        for (int j = 0; j < availablePrizes.size(); j++) {
                            Prize prize = availablePrizes.get(j);
                            if (rand <= cumulativeProbabilities.get(j)) {
                                Long stock = redisTemplate.opsForHash().increment(activityPrizeStockKey, prize.getId().toString(), -1);
                                if (stock < 0) {
                                    redisTemplate.opsForHash().increment(activityPrizeStockKey, prize.getId().toString(), 1);
                                    throw new PrizeOutOfStockException();
                                }
                                drawLogs.add(new DrawLog(userId, activityId, prize.getId(), prize.getName()));
                                break;
                            }
                        }
                        actualDrawTimes++;
                    }
                } catch (PrizeOutOfStockException e) {
                    log.warn("Prize out of stock for activityId: " + activityId + ", userId: " + userId);
                    drawLogs.add(new DrawLog(userId, activityId, null, OUT_OF_STOCK_LABEL));
                }
                if (actualDrawTimes > 0) {
                    redisTemplate.opsForHash().put(userDrawCountKey, userId, currentDraws + actualDrawTimes);
                    drawLogRepository.saveAll(drawLogs);
                }
                return drawLogs.stream()
                        .map(DrawLog::getPrizeName)
                        .collect(Collectors.toList());
            } finally {
                redisTemplate.delete(userDrawLockKey);
            }
        } else {
            throw new AppException(CustomError.USER_IS_DRAWING);
        }
    }

}
