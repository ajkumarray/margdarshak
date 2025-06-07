package com.ajkumarray.margdarshak.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;

import com.ajkumarray.margdarshak.service.UrlService;

@RestController
@RequestMapping(value = "")
public class RedirectController {

    @Autowired
    private UrlService urlService;

    @GetMapping(value = "{code}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String code) {
        String originalUrl = urlService.getOriginalUrl(code);
        if (originalUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
    }
}
