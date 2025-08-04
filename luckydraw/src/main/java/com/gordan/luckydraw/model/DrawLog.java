package com.gordan.luckydraw.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class DrawLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long drawId;
    private String userId;
    private Long activityId;
    private Long prizeId;
    private String prizeName;
    private LocalDateTime drawTime;

    public DrawLog(String userId, Long activityId, Long prizeId, String prizeName) {
        this.userId = userId;
        this.activityId = activityId;
        this.prizeId = prizeId;
        this.prizeName = prizeName;
        this.drawTime = LocalDateTime.now();
    }
}
