package com.ajkumarray.margdarshak.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * One login session (device). The refresh JWT carries this row's jti; the raw
 * token is never stored. A revoked session is soft deleted.
 */
@Entity
@Table(name = "refresh_token")
@Getter
@Setter
public class RefreshTokenEntity extends CommonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jti", nullable = false, length = 36, unique = true)
    private String jti;

    @Column(name = "user_code", nullable = false)
    private String userCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

}
