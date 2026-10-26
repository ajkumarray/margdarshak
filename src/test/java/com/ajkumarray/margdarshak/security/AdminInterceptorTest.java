package com.ajkumarray.margdarshak.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.enums.UserRoleEnums;

/**
 * The real site controllers don't exist yet, so a stub controller stands in
 * for /api/v1/site/** (admin) and /api/v1/public/site/** (public).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(AdminInterceptorTest.StubSiteController.class)
class AdminInterceptorTest {

    @RestController
    static class StubSiteController {
        @GetMapping("/api/v1/site/ping")
        String adminPing() {
            return "admin-ok";
        }

        @GetMapping("/api/v1/public/site/ping")
        String publicPing() {
            return "public-ok";
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void siteWithoutTokenIsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/site/ping")).andExpect(status().isForbidden());
    }

    @Test
    void siteWithUserTokenIsAccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/site/ping").header("Authorization", bearer(UserRoleEnums.USER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mc").value(ApplicationEnums.ACCESS_DENIED.getCode()))
                .andExpect(jsonPath("$.m").value("Access denied"));
    }

    @Test
    void siteWithAdminTokenIsAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/site/ping").header("Authorization", bearer(UserRoleEnums.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string("admin-ok"));
    }

    @Test
    void publicSiteIsOpenWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/public/site/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("public-ok"));
    }

    @Test
    void adminCanStillUseNirdeshakEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/url").header("Authorization", bearer(UserRoleEnums.ADMIN)))
                .andExpect(status().isOk());
    }

    private String bearer(UserRoleEnums role) {
        return "Bearer " + jwtTokenProvider.generateToken("MDsitetest", role);
    }
}
