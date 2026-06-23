package com.health.system.repository;

import com.health.system.entity.HealthData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthDataRepository extends JpaRepository<HealthData, Long> {

    List<HealthData> findAllByOrderByCreateTimeDesc();

    HealthData findTopByUserIdOrderByIdDesc(Long userId);

    List<HealthData> findByUserIdOrderByCreateTimeDesc(Long userId);

}
