package com.ajkumarray.margdarshak.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ajkumarray.margdarshak.entity.RefreshTokenEntity;

/**
 * All revocations are soft deletes. Each update only touches active rows and
 * returns the count, so two concurrent uses of the same token can't both
 * succeed.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    boolean existsByJtiAndUserCodeAndDeleted(String jti, String userCode, boolean deleted);

    /**
     * Rotation on refresh, or logout of one device.
     */
    @Modifying
    @Transactional
    @Query(
        """
            UPDATE RefreshTokenEntity r
            SET r.deleted = true, r.deletedAt = :now, r.updatedAt = :now
            WHERE r.jti = :jti AND r.userCode = :userCode AND r.deleted = false
        """
    )
    int revoke(@Param("jti") String jti, @Param("userCode") String userCode, @Param("now") LocalDateTime now);

    /**
     * Logout of all devices.
     */
    @Modifying
    @Transactional
    @Query(
        """
            UPDATE RefreshTokenEntity r
            SET r.deleted = true, r.deletedAt = :now, r.updatedAt = :now
            WHERE r.userCode = :userCode AND r.deleted = false
        """
    )
    int revokeAllForUser(@Param("userCode") String userCode, @Param("now") LocalDateTime now);

}
