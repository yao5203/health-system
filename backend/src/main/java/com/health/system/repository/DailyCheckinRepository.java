package com.health.system.repository;

import com.health.system.entity.DailyCheckin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyCheckinRepository extends JpaRepository<DailyCheckin, Long> {

    List<DailyCheckin> findByUserIdOrderByCheckinDateDescCreateTimeDesc(Long userId);

    List<DailyCheckin> findByUserIdAndCheckinDateBetweenOrderByCheckinDateAsc(Long userId, LocalDate start, LocalDate end);
}
