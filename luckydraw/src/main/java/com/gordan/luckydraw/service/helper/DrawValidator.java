package com.gordan.luckydraw.service.helper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.gordan.luckydraw.enums.CustomError;
import com.gordan.luckydraw.exception.AppException;
import com.gordan.luckydraw.model.Activity;
import com.gordan.luckydraw.model.Prize;

@Component
public class DrawValidator {
    public void validateMaxDrawExceeded(Activity activity, Integer currentDraws, int times) {
        int maxDraws = activity.getMaxNumberOfDrawsPerUser();

        if (currentDraws + times > maxDraws) {
            throw new AppException(CustomError.MAX_DRAWS_EXCEEDED);
        }
    }

    public void validatePrizeStock(List<Prize> availablePrizes) {
        if (availablePrizes.isEmpty()) {
            throw new AppException(CustomError.PRIZE_OUT_OF_STOCK);
        }
    }

    public void validateProbabilityExceed(double probability) {
        if (probability > 1.0) {
            throw new AppException(CustomError.PROBABILITY_EXCEED);
        }
    }
}
