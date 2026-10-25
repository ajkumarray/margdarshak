package com.ajkumarray.margdarshak.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs against the datasource in application.properties (local DB). Every test
 * is rolled back, so no rows are left behind.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * The anonymous nirdeshak endpoint must be reachable without a token.
     */
    @Test
    void anonymousPublicUrlCreateIsPermitted() throws Exception {
        mockMvc.perform(post("/api/v1/public/url").header("userCode", "MDtest123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://example.com\",\"expirationDays\":7,\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk());
    }

    /**
     * Regression guard: protected nirdeshak endpoints keep returning 403 (not
     * 401) when no token is sent.
     */
    @Test
    void protectedUrlEndpointWithoutTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/url").contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"https://example.com\"}")).andExpect(status().isForbidden());
    }
}
