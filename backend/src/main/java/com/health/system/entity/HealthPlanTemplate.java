package com.health.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "health_plan_template")
public class HealthPlanTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_type")
    private String templateType;

    private String name;

    @Column(name = "constitution_type")
    private String constitutionType;

    @Column(name = "risk_level")
    private String riskLevel;

    private String dimension;

    @Column(name = "min_score")
    private Integer minScore;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer priority;

    @Column(name = "is_active")
    private Integer isActive;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = 1;
        }
        if (priority == null) {
            priority = 0;
        }
    }
}
