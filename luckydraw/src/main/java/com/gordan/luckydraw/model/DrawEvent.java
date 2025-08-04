package com.gordan.luckydraw.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "draw_events")
@Data
@NoArgsConstructor
public class DrawEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long drawId;
    private String userId;
    private Long activityId;
    private Long prizeId;
    private String prizeName;
    private LocalDateTime drawTime;

    public DrawEvent(String userId, Long activityId, Long prizeId, String prizeName) {
        this.userId = userId;
        this.activityId = activityId;
        this.prizeId = prizeId;
        this.prizeName = prizeName;
        this.drawTime = LocalDateTime.now();
    }
}
