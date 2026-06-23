package com.health.system.repository;

import com.health.system.entity.QuestionnaireQuality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionnaireQualityRepository extends JpaRepository<QuestionnaireQuality, Long> {

    List<QuestionnaireQuality> findByUserIdOrderByCreateTimeDesc(Long userId);
}
