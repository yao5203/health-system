package com.health.system.repository;

import com.health.system.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 获取所有题目（其实JpaRepository自带，但写上更清晰）
    List<Question> findAll();

    List<Question> findAllByOrderByIdAsc();

    List<Question> findAllByOrderBySortOrderAscIdAsc();

    List<Question> findByIsActiveOrderBySortOrderAscIdAsc(Integer isActive);

    // 根据体质分类查题目（后面可能用到）
    List<Question> findByCategory(String category);
}
