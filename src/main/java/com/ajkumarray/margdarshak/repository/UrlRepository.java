package com.ajkumarray.margdarshak.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ajkumarray.margdarshak.entity.Url;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortUrl(String shortUrl);
    boolean existsByShortUrl(String shortUrl);
} 