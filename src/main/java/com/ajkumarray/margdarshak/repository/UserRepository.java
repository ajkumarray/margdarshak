package com.ajkumarray.margdarshak.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ajkumarray.margdarshak.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUserCode(String userCode);
    boolean existsByEmail(String email);
    boolean existsByUserCode(String userCode);
} 