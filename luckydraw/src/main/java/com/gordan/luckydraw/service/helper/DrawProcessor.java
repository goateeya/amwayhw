package com.gordan.luckydraw.service.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gordan.luckydraw.enums.DrawResult;
import com.gordan.luckydraw.exception.PrizeOutOfStockException;
import com.gordan.luckydraw.model.DrawEvent;
import com.gordan.luckydraw.model.Prize;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DrawProcessor {
    private static final Random RANDOM = new Random();

    @Autowired
    private PrizeStockHandler prizeStockHandler;

    public List<DrawEvent> performDraws(Long activityId, String userId, int drawTimes,
            Map<Prize, Double> cumulativeProbabilityMap) {
        List<DrawEvent> drawLogs = new ArrayList<>();
        try {
            for (int i = 0; i < drawTimes; i++) {
                double rand = RANDOM.nextDouble(); // 0~1
                boolean wonPrize = false;
                for (Map.Entry<Prize, Double> entry : cumulativeProbabilityMap.entrySet()) {
                    Prize prize = entry.getKey();
                    double cumulative = entry.getValue();
                    if (rand <= cumulative) {
                        prizeStockHandler.decrementPrizeStockIfAvailable(activityId, prize);
                        drawLogs.add(new DrawEvent(userId, activityId, prize.getId(), prize.getName()));
                        wonPrize = true;
                        break;
                    }
                }
                if (!wonPrize) {
                    drawLogs.add(new DrawEvent(userId, activityId, null, DrawResult.NO_PRIZE.getLabel()));
                }
            }
        } catch (PrizeOutOfStockException e) {
            log.warn("Prize out of stock for activityId: " + activityId + ", userId: " +
                    userId);
            drawLogs.add(new DrawEvent(userId, activityId, null, DrawResult.OUT_OF_STOCK.name()));
        }
        return drawLogs;
    }
}
