package com.gordan.luckydraw.service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.gordan.luckydraw.enums.CustomError;
import com.gordan.luckydraw.enums.RedisKeyTemplate;
import com.gordan.luckydraw.exception.AppException;
import com.gordan.luckydraw.model.Activity;
import com.gordan.luckydraw.model.DrawEvent;
import com.gordan.luckydraw.model.Prize;
import com.gordan.luckydraw.repository.ActivityRepository;
import com.gordan.luckydraw.repository.DrawEventRepository;
import com.gordan.luckydraw.repository.PrizeRepository;
import com.gordan.luckydraw.service.helper.DrawProcessor;
import com.gordan.luckydraw.service.helper.DrawValidator;
import com.gordan.luckydraw.service.helper.PrizeStockHandler;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class LotteryService {
    private static final int MAX_DRAW_LOCK_DURATION_SECONDS = 30;
    @Autowired
    private PrizeRepository prizeRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private DrawEventRepository drawEventRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DrawValidator drawValidator;

    @Autowired
    private PrizeStockHandler prizeStockHandler;

    @Autowired
    private DrawProcessor drawProcessor;

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
        drawValidator.validateProbabilityExceed(probability);

        return prizeRepository.saveAll(
                prizes.stream().map(prize -> {
                    prize.setActivityId(activityId);
                    return prize;
                }).toList());
    }

    public Activity createActivity(com.gordan.luckydraw.model.Activity activity) {
        return activityRepository.save(activity);
    }

    public List<String> draw(Long activityId, String userId, int drawTimes) {
        // 實現抽獎邏輯
        String userDrawLockKey = RedisKeyTemplate.USER_DRAW_LOCK_KEY.format(activityId,
                userId);
        if (!tryToGetUserDrawLock(userDrawLockKey)) {
            throw new AppException(CustomError.USER_IS_DRAWING);
        }

        try {
            Activity activity = activityRepository.findById(activityId)
                    .orElseThrow(() -> new AppException(CustomError.ACTIVITY_NOT_FOUND));
            Integer currentDraws = Optional.ofNullable(redisTemplate.opsForHash()
                    .get(RedisKeyTemplate.USER_DRAW_COUNT_KEY.format(activity.getId()), userId))
                    .map(Integer.class::cast)
                    .orElse(0);
            drawValidator.validateMaxDrawExceeded(activity, currentDraws, drawTimes);

            List<Prize> availablePrizes = prizeStockHandler
                    .getAvailablePrizes(prizeStockHandler.getPrizeStocks(activityId));
            drawValidator.validatePrizeStock(availablePrizes);

            // Precompute cumulative probabilities for available prizes using Map<Prize, Double>
            Map<Prize, Double> cumulativeProbabilityMap = new LinkedHashMap<>();
            double cumulative = 0;
            for (Prize prize : availablePrizes) {
                cumulative += prize.getProbability();
                cumulativeProbabilityMap.put(prize, cumulative);
            }
            drawValidator.validateProbabilityExceed(cumulative);

            List<DrawEvent> drawEvents = drawProcessor.performDraws(activityId, userId, drawTimes, cumulativeProbabilityMap);
            if (!drawEvents.isEmpty()) {
                redisTemplate.opsForHash().put(RedisKeyTemplate.USER_DRAW_COUNT_KEY.format(activity.getId()),
                        userId, currentDraws + drawEvents.size());
                drawEventRepository.saveAll(drawEvents);
            }
            return drawEvents.stream()
                    .map(DrawEvent::getPrizeName)
                    .toList();
        } finally {
            redisTemplate.delete(userDrawLockKey);
        }
    }

    private boolean tryToGetUserDrawLock(String lockKey) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCK",
                Duration.ofSeconds(MAX_DRAW_LOCK_DURATION_SECONDS)));
    }
}
