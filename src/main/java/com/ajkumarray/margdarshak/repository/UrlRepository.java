package com.ajkumarray.margdarshak.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ajkumarray.margdarshak.entity.UrlEntity;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
    Optional<UrlEntity> findByShortUrl(String shortUrl);
    boolean existsByShortUrl(String shortUrl);
} 