package com.health.system.repository;

import com.health.system.entity.HealthPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthPlanRepository extends JpaRepository<HealthPlan, Long> {

    /**
     * 根据体质 + 健康等级查询康养方案
     * 对应SQL：
     * SELECT * FROM health_plan
     * WHERE constitution_type = ? AND health_level = ?
     */
    HealthPlan findTopByConstitutionTypeAndHealthLevelOrderByCreateTimeDesc(String constitutionType, String healthLevel);

    boolean existsByConstitutionTypeAndHealthLevel(String constitutionType, String healthLevel);

    HealthPlan findTopByConstitutionTypeOrderByCreateTimeDesc(String constitutionType);

}
