package com.health.system.repository;

import com.health.system.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    // 根据题目ID获取选项
    List<QuestionOption> findByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);
}
