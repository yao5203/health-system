package com.health.system.repository;

import com.health.system.entity.HealthResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthResultRepository extends JpaRepository<HealthResult, Long> {

    List<HealthResult> findByUserIdOrderByCreateTimeDesc(Long userId);

    HealthResult findTopByUserIdOrderByCreateTimeDesc(Long userId);
}
