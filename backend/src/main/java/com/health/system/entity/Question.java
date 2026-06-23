package com.health.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 题目内容
    private String content;

    // 1 正向题  2 反向题
    private Integer type;

    // 体质分类（气虚/湿热/阴虚等）
    private String category;

    // 评估维度：身体健康状况/生活习惯评估/心理健康状况/睡眠质量评估
    private String dimension;

    @Column(name = "applicable_constitution")
    private String applicableConstitution;

    @Column(name = "applicable_health_level")
    private String applicableHealthLevel;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_active")
    private Integer isActive;

    // 创建时间
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (isActive == null) {
            isActive = 1;
        }
        if (applicableConstitution == null || applicableConstitution.isBlank()) {
            applicableConstitution = "通用";
        }
        if (applicableHealthLevel == null || applicableHealthLevel.isBlank()) {
            applicableHealthLevel = "通用";
        }
    }
}
