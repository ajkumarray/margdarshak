package com.ajkumarray.margdarshak.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ajkumarray.margdarshak.entity.UserMasterEntity;
import com.ajkumarray.margdarshak.enums.UserRoleEnums;
import com.ajkumarray.margdarshak.models.request.UserMasterRequest;
import com.ajkumarray.margdarshak.models.response.AuthResponse;
import com.ajkumarray.margdarshak.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class UserHelperTest {

    @Mock
    private CommonFunctionHelper commonFunctionHelper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserHelper userHelper;

    @Test
    void newUserIsAlwaysUser() {
        when(commonFunctionHelper.generateAlphaNumericCode(8)).thenReturn("MDuser001");
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        UserMasterRequest request = new UserMasterRequest();
        request.setEmail("t@margdarshak.test");
        request.setPassword("secret123");

        UserMasterEntity user = userHelper.prepareUserEntity(request);

        assertThat(user.getRole()).isEqualTo(UserRoleEnums.USER);
        assertThat(user.getPassword()).isEqualTo("hashed");
    }

    @Test
    void authResponseCarriesRoleAndBothTokens() {
        UserMasterEntity admin = new UserMasterEntity();
        admin.setUserCode("MDadmin01");
        admin.setRole(UserRoleEnums.ADMIN);
        when(jwtTokenProvider.generateToken("MDadmin01", UserRoleEnums.ADMIN)).thenReturn("access");

        AuthResponse response = userHelper.prepareAuthResponse(admin, "refresh");

        assertThat(response.getToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        assertThat(response.getUserDetails().getRole()).isEqualTo(UserRoleEnums.ADMIN);
    }
}
