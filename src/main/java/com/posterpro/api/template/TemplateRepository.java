package com.posterpro.api.template;

import com.posterpro.api.common.PlanTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByCategoryIdAndIsActiveTrue(Long categoryId);
    List<Template> findByPlanTierMinAndIsActiveTrue(PlanTier planTier);
    List<Template> findByIsFestivalTrueAndIsActiveTrue();

    @Query("""
            SELECT t FROM Template t
            WHERE t.isActive = true
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:search = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Template> search(@Param("categoryId") Long categoryId, @Param("search") String search, Pageable pageable);
}
