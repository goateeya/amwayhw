package com.gordan.luckydraw.model;

import org.springframework.data.annotation.Id;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Activity {
    @Id
    private Long id;
    private String name;
    private int maxNumberOfDrawsPerUser;
}
