package com.health.system.dto;

import com.health.system.entity.HealthResult;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserHealthResultView {

    private Long id;
    private Long userId;
    private String username;
    private Integer age;
    private String gender;
    private String phone;
    private Double score;
    private String healthLevel;
    private String constitutionType;
    private LocalDateTime createTime;

    public static UserHealthResultView from(HealthResult result, String username, Integer age, String gender, String phone) {
        return new UserHealthResultView(
                result.getId(),
                result.getUserId(),
                username,
                age,
                gender,
                phone,
                result.getScore(),
                result.getHealthLevel(),
                result.getConstitutionType(),
                result.getCreateTime()
        );
    }
}
