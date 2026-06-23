package com.health.system.repository;

import com.health.system.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    // 根据用户ID查询答题记录
    List<UserAnswer> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
