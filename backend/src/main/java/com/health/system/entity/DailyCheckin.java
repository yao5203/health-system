package com.health.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "daily_checkin")
public class DailyCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "checkin_date")
    private LocalDate checkinDate;

    @Column(name = "plan_title")
    private String planTitle;

    @Column(name = "sleep_hours")
    private Double sleepHours;

    @Column(name = "exercise_minutes")
    private Integer exerciseMinutes;

    @Column(name = "stress_level")
    private Integer stressLevel;

    @Column(name = "mood_score")
    private Integer moodScore;

    private Double weight;

    @Column(name = "blood_pressure_high")
    private Integer bloodPressureHigh;

    @Column(name = "blood_pressure_low")
    private Integer bloodPressureLow;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (checkinDate == null) {
            checkinDate = LocalDate.now();
        }
    }
}
