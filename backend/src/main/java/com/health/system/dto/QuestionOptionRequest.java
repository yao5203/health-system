package com.health.system.dto;

import lombok.Data;

@Data
public class QuestionOptionRequest {

    private Long id;

    private String content;

    private Integer score;
}
