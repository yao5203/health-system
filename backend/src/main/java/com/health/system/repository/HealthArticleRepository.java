package com.health.system.repository;

import com.health.system.entity.HealthArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HealthArticleRepository extends JpaRepository<HealthArticle, Long> {

    List<HealthArticle> findAllByOrderByCreateTimeDesc();

    List<HealthArticle> findByStatusOrderByCreateTimeDesc(Integer status);

    List<HealthArticle> findByStatusAndCategoryContainingIgnoreCaseOrderByCreateTimeDesc(Integer status, String category);

    List<HealthArticle> findByStatusAndTitleContainingIgnoreCaseOrderByCreateTimeDesc(Integer status, String title);

    List<HealthArticle> findByStatusAndCategoryContainingIgnoreCaseAndTitleContainingIgnoreCaseOrderByCreateTimeDesc(
            Integer status, String category, String title
    );

    @Query("""
            select a from HealthArticle a
            where a.status = :status
              and (:category = '' or lower(a.category) like lower(concat('%', :category, '%')))
              and (
                    :keyword = ''
                    or lower(a.title) like lower(concat('%', :keyword, '%'))
                    or lower(a.summary) like lower(concat('%', :keyword, '%'))
                    or lower(a.content) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(a.tags, '')) like lower(concat('%', :keyword, '%'))
                    or lower(a.category) like lower(concat('%', :keyword, '%'))
                  )
            order by a.createTime desc
            """)
    List<HealthArticle> searchPublished(@Param("status") Integer status,
                                        @Param("category") String category,
                                        @Param("keyword") String keyword);
}
