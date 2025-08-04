package com.gordan.luckydraw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gordan.luckydraw.model.Prize;

public interface PrizeRepository extends JpaRepository<Prize, Long> {
    public List<Prize> findByActivityId(Long activityId);
}
