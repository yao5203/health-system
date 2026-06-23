package com.health.system.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyCheckinRequest {
    private Long userId;
    private LocalDate checkinDate;
    private String planTitle;
    private Double sleepHours;
    private Integer exerciseMinutes;
    private Integer stressLevel;
    private Integer moodScore;
    private Double weight;
    private Integer bloodPressureHigh;
    private Integer bloodPressureLow;
    private String remark;
}
