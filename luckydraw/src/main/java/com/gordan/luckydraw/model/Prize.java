package com.gordan.luckydraw.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Prize {
    @Id
    private Long id;
    private String name;
    private int stock;
    private Double probability;
    private Long activityId;
    @Version
    private Long version;
}
