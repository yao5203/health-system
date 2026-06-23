package com.health.system.dto;

import lombok.Data;

@Data
public class ConsultationMessageRequest {
    private String senderType;
    private Long senderId;
    private String senderName;
    private String content;
}
