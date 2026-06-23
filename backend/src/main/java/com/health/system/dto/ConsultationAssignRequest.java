package com.health.system.dto;

import lombok.Data;

@Data
public class ConsultationAssignRequest {
    private Long doctorId;
    private Long adminId;
    private String adminName;
    private String adminNote;
}
