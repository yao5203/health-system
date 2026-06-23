package com.health.system.repository;

import com.health.system.entity.PlanFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanFavoriteRepository extends JpaRepository<PlanFavorite, Long> {

    List<PlanFavorite> findByUserIdOrderByCreateTimeDesc(Long userId);

    boolean existsByUserIdAndPlanId(Long userId, Long planId);

    void deleteByUserIdAndPlanId(Long userId, Long planId);
}
