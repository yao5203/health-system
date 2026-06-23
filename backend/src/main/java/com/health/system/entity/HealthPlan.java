package com.health.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "health_plan")
public class HealthPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "constitution_type")
    private String constitutionType;

    @Column(name = "health_level")
    private String healthLevel;

    @Column(columnDefinition = "TEXT")
    private String diet;

    @Column(columnDefinition = "TEXT")
    private String drink;

    @Column(columnDefinition = "TEXT")
    private String sport;

    @Column(columnDefinition = "TEXT")
    private String lifestyle;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
