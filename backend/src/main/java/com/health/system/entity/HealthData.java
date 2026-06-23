package com.health.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "health_data")
public class HealthData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "blood_pressure_high")
    private Integer bloodPressureHigh;

    @Column(name = "blood_pressure_low")
    private Integer bloodPressureLow;

    @Column(name = "blood_pressure_context")
    private String bloodPressureContext;

    private Double height;
    private Double weight;

    private Double bloodSugar;

    @Column(name = "blood_sugar_context")
    private String bloodSugarContext;

    private Integer heartRate;

    @Column(name = "heart_rate_context")
    private String heartRateContext;

    private Double bloodOxygen;

    @Column(name = "blood_oxygen_context")
    private String bloodOxygenContext;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
