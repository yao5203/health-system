package com.health.system.repository;

import com.health.system.entity.HealthPlanTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthPlanTemplateRepository extends JpaRepository<HealthPlanTemplate, Long> {

    List<HealthPlanTemplate> findByTemplateTypeAndIsActiveOrderByPriorityDescCreateTimeDesc(String templateType,
                                                                                           Integer isActive);
}
