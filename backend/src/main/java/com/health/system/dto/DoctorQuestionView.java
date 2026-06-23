package com.health.system.dto;

import com.health.system.entity.Question;
import com.health.system.entity.QuestionOption;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class DoctorQuestionView {

    private Long id;
    private String content;
    private Integer type;
    private String category;
    private String dimension;
    private String applicableConstitution;
    private String applicableHealthLevel;
    private Integer sortOrder;
    private Integer isActive;
    private LocalDateTime createTime;
    private List<QuestionOption> options;

    public static DoctorQuestionView from(Question question, List<QuestionOption> options) {
        return new DoctorQuestionView(
                question.getId(),
                question.getContent(),
                question.getType(),
                question.getCategory(),
                question.getDimension(),
                question.getApplicableConstitution(),
                question.getApplicableHealthLevel(),
                question.getSortOrder(),
                question.getIsActive(),
                question.getCreateTime(),
                options
        );
    }
}
