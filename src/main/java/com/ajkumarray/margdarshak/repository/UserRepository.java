package com.ajkumarray.margdarshak.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ajkumarray.margdarshak.entity.UserMasterEntity;
import com.ajkumarray.margdarshak.enums.UserStatusEnums;

@Repository
public interface UserRepository extends JpaRepository<UserMasterEntity, Long> {

    Optional<UserMasterEntity> findByUserCodeAndStatusAndDeleted(String userCode, UserStatusEnums status,
            boolean deleted);

    Optional<UserMasterEntity> findByEmail(String email);

}
