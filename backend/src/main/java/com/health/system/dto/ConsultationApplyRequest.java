package com.health.system.dto;

import lombok.Data;

@Data
public class ConsultationApplyRequest {
    private Long userId;
    private String issueType;
    private String title;
    private String detail;
    private String preferredTag;
}
