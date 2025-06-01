package com.ajkumarray.margdarshak.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.ajkumarray.margdarshak.enums.UrlStatusEnums;

/**
 * Entity class representing a URL in the system. Contains information about
 * both the original and shortened URLs.
 */
@Entity
@Table(name = "urls")
@Getter
@Setter
@ToString
public class UrlMasterEntity extends CommonEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "short_url", nullable = false)
    private String shortUrl;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "click_count", nullable = false)
    private Long clickCount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UrlStatusEnums status;

    @Column(name = "last_accessed_at", nullable = true)
    private LocalDateTime lastAccessedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

}