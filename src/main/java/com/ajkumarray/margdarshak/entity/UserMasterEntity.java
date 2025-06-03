package com.ajkumarray.margdarshak.entity;

import com.ajkumarray.margdarshak.enums.UserStatusEnums;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_master")
@Getter
@Setter
public class UserMasterEntity extends CommonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userCode", nullable = false)
    private String userCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "profilePic", nullable = true)
    private String profilePic;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatusEnums status;

}