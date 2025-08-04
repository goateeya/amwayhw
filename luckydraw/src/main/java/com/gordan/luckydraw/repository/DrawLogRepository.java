package com.gordan.luckydraw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gordan.luckydraw.model.DrawLog;

public interface DrawLogRepository extends JpaRepository<DrawLog, Long> {
}
