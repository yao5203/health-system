package com.health.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "plan_favorite")
public class PlanFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "plan_title")
    private String planTitle;

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
