package com.ajkumarray.margdarshak.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ajkumarray.margdarshak.entity.UrlMasterEntity;
import com.ajkumarray.margdarshak.enums.UrlStatusEnums;

@Repository
public interface UrlRepository extends JpaRepository<UrlMasterEntity, Long> {
    Page<UrlMasterEntity> findAllByCreatedByAndStatusAndDeleted(String createdBy, UrlStatusEnums status,
            boolean deleted, Pageable pageable);

    Optional<UrlMasterEntity> findByCodeAndStatusAndDeleted(String code, UrlStatusEnums status, boolean deleted);

    Optional<UrlMasterEntity> findByCodeAndStatusAndExpiresAtAfterAndDeleted(String code, UrlStatusEnums status,
            LocalDateTime expiresAt, boolean deleted);

}