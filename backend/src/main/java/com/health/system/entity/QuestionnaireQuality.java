package com.health.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "questionnaire_quality")
public class QuestionnaireQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "answered_questions")
    private Integer answeredQuestions;

    @Column(name = "missing_count")
    private Integer missingCount;

    @Column(name = "straight_line_rate")
    private Double straightLineRate;

    @Column(name = "quality_score")
    private Integer qualityScore;

    @Column(name = "consistency_level")
    private String consistencyLevel;

    @Column(columnDefinition = "TEXT")
    private String warnings;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
