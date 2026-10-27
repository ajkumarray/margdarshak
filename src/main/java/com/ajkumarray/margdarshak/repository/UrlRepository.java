package com.ajkumarray.margdarshak.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ajkumarray.margdarshak.entity.UrlMasterEntity;
import com.ajkumarray.margdarshak.enums.UrlStatusEnums;

@Repository
public interface UrlRepository extends JpaRepository<UrlMasterEntity, Long> {
    Page<UrlMasterEntity> findAllByCreatedByAndStatusAndDeleted(String createdBy, UrlStatusEnums status,
            boolean deleted, Pageable pageable);

    Optional<UrlMasterEntity> findByCodeAndStatusAndDeleted(String code, UrlStatusEnums status, boolean deleted);

    Optional<UrlMasterEntity> findByCodeAndStatusAndExpiresAtAfterAndDeleted(String code, UrlStatusEnums status,
            LocalDateTime expiresAt, boolean deleted);

    boolean existsByCode(String code);

    /**
     * Single atomic UPDATE so concurrent redirects don't overwrite each other's increments.
     */
    @Modifying
    @Transactional
    @Query(
        """
            UPDATE UrlMasterEntity u
            SET u.clickCount = u.clickCount + 1, u.lastAccessedAt = :accessedAt
            WHERE u.id = :id
        """
    )
    int incrementClickCount(@Param("id") Long id, @Param("accessedAt") LocalDateTime accessedAt);

}