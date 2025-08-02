package com.gordan.luckydraw.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gordan.luckydraw.enums.CustomError;
import com.gordan.luckydraw.exception.AppException;
import com.gordan.luckydraw.model.Prize;
import com.gordan.luckydraw.repository.ActivityRepository;
import com.gordan.luckydraw.repository.PrizeRepository;

@Service
public class LotteryService {
    @Autowired
    private PrizeRepository prizeRepository;

    @Autowired
    private ActivityRepository activityRepository;

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

        // 實現獎品設定邏輯
        for (Prize prize : prizes) {
            prize.setActivityId(activityId);
            prizeRepository.save(prize);
        }
    }

    public List<String> draw(Long activityId, String userId, int times) {
        // 實現抽獎邏輯
        return List.of(); // 返回抽獎結果
    }

}
