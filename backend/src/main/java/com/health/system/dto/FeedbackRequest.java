package com.health.system.dto;

import lombok.Data;

@Data
public class FeedbackRequest {

    private Long userId;
    private String content;
}
