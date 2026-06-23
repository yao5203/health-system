package com.health.system.dto;

import lombok.Data;

@Data
public class ProposalSubmitRequest {
    private Long proposerDoctorId;
    private String proposerName;
    private String targetType;
    private String actionType;
    private Long targetId;
    private String title;
    private String summary;
    private String payloadJson;
}
