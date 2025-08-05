package com.gordan.luckydraw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gordan.luckydraw.model.DrawEvent;

public interface DrawEventRepository extends JpaRepository<DrawEvent, Long> {
}
