package com.health.system.dto;

import com.health.system.entity.UserAnswer;
import lombok.Data;

import java.util.List;

@Data
public class QuestionnaireSubmitRequest {

    private List<UserAnswer> answers;

    private Integer durationSeconds;

    private Long startedAt;

    private Long completedAt;
}
