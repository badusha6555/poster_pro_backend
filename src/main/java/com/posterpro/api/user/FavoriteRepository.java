package com.posterpro.api.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findByUserId(Long userId);
    Page<Favorite> findByUserId(Long userId, Pageable pageable);
    boolean existsByIdUserIdAndIdTemplateId(Long userId, Long templateId);

    @Query("SELECT f.id.templateId FROM Favorite f WHERE f.id.userId = :userId")
    Set<Long> findTemplateIdsByUserId(@Param("userId") Long userId);
}
