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
        String userDrawLockKey = "DRAW_LOCK_" + activityId + "_" + userId;
        if (redisTemplate.opsForValue().setIfAbsent(userDrawLockKey, "LOCK", Duration.ofSeconds(times))) {
            try {
                // 檢查用戶是否達到最大抽獎次數
                Activity activity = activityRepository.findById(activityId)
                        .orElseThrow(() -> new AppException(CustomError.ACTIVITY_NOT_FOUND));
                int maxDraws = activity.getMaxNumberOfDrawsPerUser();

                // 檢查用戶當前抽獎次數
                String key = "ACT_" + activityId + "_USER_DRAW_COUNT";
                Integer currentDraws = (Integer) redisTemplate.opsForHash().get(key, userId);
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

                List<String> results = new ArrayList<>();
                Random random = new Random();

                // Precompute cumulative probabilities
                List<Double> cumulativeProbabilities = new ArrayList<>();
                double cumulative = 0;
                for (Prize p : prizes) {
                    cumulative += p.getProbability();
                    cumulativeProbabilities.add(cumulative);
                }

                for (int i = 0; i < times; i++) {
                    double rand = random.nextDouble();
                    String result = "銘謝惠顧";
                    for (int j = 0; j < prizes.size(); j++) {
                        Prize p = prizes.get(j);
                        String prizeKey = "ACT_" + activityId + "_PRIZE_STOCK";
                        if (rand <= cumulativeProbabilities.get(j)) {
                            Long stock = redisTemplate.opsForHash().increment(prizeKey, p.getId(), -1);
                            if (stock < 0) {
                                redisTemplate.opsForHash().increment(prizeKey, p.getId(), 1);
                                break;
                            }
                            result = p.getName();
                            break;
                        }
                    }
                    results.add(result);
                }
                redisTemplate.opsForHash().put(key, userId, currentDraws + times);
                return results;
            } finally {
                redisTemplate.delete(userDrawLockKey);
            }
        } else {
            throw new AppException(CustomError.USER_IS_DRAWING);
        }
    }

}
