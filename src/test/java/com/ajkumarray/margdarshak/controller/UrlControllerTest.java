package com.ajkumarray.margdarshak.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ajkumarray.margdarshak.enums.UserRoleEnums;
import com.jayway.jsonpath.JsonPath;
import com.ajkumarray.margdarshak.security.JwtTokenProvider;

/**
 * Runs against the datasource in application.properties (local DB). Every test
 * is rolled back, so no rows are left behind.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UrlControllerTest {

    private static final String USER_CODE = "MDurltest1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createAndListUseUserCodeFromToken() throws Exception {
        String bearer = "Bearer " + jwtTokenProvider.generateToken(USER_CODE, UserRoleEnums.USER);

        mockMvc.perform(post("/api/v1/url").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://example.com\",\"expirationDays\":7,\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lc.createdBy").value(USER_CODE));

        mockMvc.perform(get("/api/v1/url").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lc.content[0].createdBy").value(USER_CODE));
    }

    @Test
    void redirectIncrementsClickCountAndSetsLastAccessedAt() throws Exception {
        String bearer = "Bearer " + jwtTokenProvider.generateToken(USER_CODE, UserRoleEnums.USER);
        String body = mockMvc.perform(post("/api/v1/url").header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://example.com\",\"expirationDays\":7,\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String code = JsonPath.read(body, "$.lc.code");

        mockMvc.perform(get("/" + code)).andExpect(status().isFound());
        mockMvc.perform(get("/" + code)).andExpect(status().isFound());

        Long clicks = jdbcTemplate.queryForObject("SELECT click_count FROM url_master WHERE code = ?", Long.class,
                code);
        Object lastAccessedAt = jdbcTemplate.queryForObject("SELECT last_accessed_at FROM url_master WHERE code = ?",
                Object.class, code);
        assertThat(clicks).isEqualTo(2L);
        assertThat(lastAccessedAt).isNotNull();
    }
}
