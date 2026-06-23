package com.health.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {

    private String content;

    private Integer type;

    private String category;

    private String dimension;

    private String applicableConstitution;

    private String applicableHealthLevel;

    private Integer sortOrder;

    private Integer isActive;

    private List<QuestionOptionRequest> options;
}
