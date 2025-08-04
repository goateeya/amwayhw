package com.gordan.luckydraw.model;

import jakarta.persistence.Id;
import jakarta.persistence.Version;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;

@Entity
@Data
public class Prize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int stock;
    private Double probability;
    private Long activityId;
    @Version
    private Long version;
}
