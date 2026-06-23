package com.health.system.dto;

import lombok.Data;

@Data
public class DoctorProfileRequest {
    private String specialty;
    private String expertiseTags;
    private String introduction;
    private Integer status;
}
