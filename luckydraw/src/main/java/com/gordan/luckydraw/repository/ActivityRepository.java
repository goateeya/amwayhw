package com.gordan.luckydraw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gordan.luckydraw.model.Activity;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}