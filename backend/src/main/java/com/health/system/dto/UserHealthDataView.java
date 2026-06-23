package com.health.system.dto;

import com.health.system.entity.HealthData;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserHealthDataView {

    private Long id;
    private Long userId;
    private String username;
    private Integer age;
    private String gender;
    private String phone;
    private Integer bloodPressureHigh;
    private Integer bloodPressureLow;
    private String bloodPressureContext;
    private Double bloodSugar;
    private String bloodSugarContext;
    private Integer heartRate;
    private String heartRateContext;
    private Double bloodOxygen;
    private String bloodOxygenContext;
    private Double height;
    private Double weight;
    private LocalDateTime createTime;

    public static UserHealthDataView from(HealthData data, String username, Integer age, String gender, String phone) {
        return new UserHealthDataView(
                data.getId(),
                data.getUserId(),
                username,
                age,
                gender,
                phone,
                data.getBloodPressureHigh(),
                data.getBloodPressureLow(),
                data.getBloodPressureContext(),
                data.getBloodSugar(),
                data.getBloodSugarContext(),
                data.getHeartRate(),
                data.getHeartRateContext(),
                data.getBloodOxygen(),
                data.getBloodOxygenContext(),
                data.getHeight(),
                data.getWeight(),
                data.getCreateTime()
        );
    }
}
