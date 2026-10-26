package com.ajkumarray.margdarshak.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ajkumarray.margdarshak.entity.RefreshTokenEntity;
import com.ajkumarray.margdarshak.entity.UserMasterEntity;
import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.enums.UserStatusEnums;
import com.ajkumarray.margdarshak.exception.ApplicationException;
import com.ajkumarray.margdarshak.models.request.UserLoginRequest;
import com.ajkumarray.margdarshak.models.request.UserMasterRequest;
import com.ajkumarray.margdarshak.models.response.AuthResponse;
import com.ajkumarray.margdarshak.repository.RefreshTokenRepository;
import com.ajkumarray.margdarshak.repository.UserRepository;
import com.ajkumarray.margdarshak.security.JwtTokenProvider;
import com.ajkumarray.margdarshak.util.CommonFunctionHelper;
import com.ajkumarray.margdarshak.util.UserHelper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserImplementationTest {

    private static final String USER_CODE = "MDuser001";
    private static final String JTI = "jti-1";
    private static final String EMAIL = "t@margdarshak.test";

    @Mock
    private UserHelper userHelper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommonFunctionHelper commonFunctionHelper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserImplementation userImplementation;

    private UserMasterEntity user;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        user = new UserMasterEntity();
        user.setUserCode(USER_CODE);
        user.setPassword("hashed");
        authResponse = new AuthResponse();

        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(604800L);
        when(jwtTokenProvider.generateRefreshToken(eq(USER_CODE), anyString(), any())).thenReturn("refresh");
        when(userHelper.prepareRefreshTokenEntity(eq(USER_CODE), anyString(), any()))
                .thenAnswer(call -> session(call.getArgument(1)));
        when(userHelper.prepareAuthResponse(user, "refresh")).thenReturn(authResponse);
    }

    @Test
    void registerRejectsExistingEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertError(() -> userImplementation.register(registerRequest()), ApplicationEnums.USER_ALREADY_EXISTS);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerSavesUserAndStartsSession() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userHelper.prepareUserEntity(any())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        assertThat(userImplementation.register(registerRequest())).isSameAs(authResponse);
        assertSessionStartedWithSameJti();
    }

    @Test
    void loginWithWrongPasswordFails() {
        givenActiveUser();
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertError(() -> userImplementation.login(loginRequest("wrong")), ApplicationEnums.LOGIN_FAILED);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginForBlockedOrDeletedUserFails() {
        when(userRepository.findByEmailAndStatusAndDeleted(EMAIL, UserStatusEnums.ACTIVE, false))
                .thenReturn(Optional.empty());

        assertError(() -> userImplementation.login(loginRequest("secret123")), ApplicationEnums.LOGIN_FAILED);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginStartsSession() {
        givenActiveUser();
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);

        assertThat(userImplementation.login(loginRequest("secret123"))).isSameAs(authResponse);
        assertSessionStartedWithSameJti();
    }

    @Test
    void refreshRevokesOldSessionAndStartsNewOne() {
        when(refreshTokenRepository.revoke(eq(JTI), eq(USER_CODE), any())).thenReturn(1);
        when(userRepository.findByUserCodeAndStatusAndDeleted(USER_CODE, UserStatusEnums.ACTIVE, false))
                .thenReturn(Optional.of(user));

        assertThat(userImplementation.refresh(USER_CODE, JTI)).isSameAs(authResponse);
        assertSessionStartedWithSameJti();
    }

    /**
     * Unknown session, another user's session or an already used token: the
     * atomic revoke matches no active row.
     */
    @Test
    void refreshWithInactiveSessionFails() {
        when(refreshTokenRepository.revoke(eq(JTI), eq(USER_CODE), any())).thenReturn(0);

        assertError(() -> userImplementation.refresh(USER_CODE, JTI), ApplicationEnums.INVALID_REFRESH_TOKEN);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshForBlockedUserFails() {
        when(refreshTokenRepository.revoke(eq(JTI), eq(USER_CODE), any())).thenReturn(1);
        when(userRepository.findByUserCodeAndStatusAndDeleted(USER_CODE, UserStatusEnums.ACTIVE, false))
                .thenReturn(Optional.empty());

        assertError(() -> userImplementation.refresh(USER_CODE, JTI), ApplicationEnums.INVALID_REFRESH_TOKEN);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logoutRevokesOnlyThatSession() {
        userImplementation.logout(USER_CODE, JTI, false);

        verify(refreshTokenRepository).revoke(eq(JTI), eq(USER_CODE), any());
        verify(refreshTokenRepository, never()).revokeAllForUser(anyString(), any());
    }

    @Test
    void logoutAllRevokesEverySession() {
        when(refreshTokenRepository.existsByJtiAndUserCodeAndDeleted(JTI, USER_CODE, false)).thenReturn(true);

        userImplementation.logout(USER_CODE, JTI, true);

        verify(refreshTokenRepository).revokeAllForUser(eq(USER_CODE), any());
    }

    @Test
    void logoutAllWithInactiveSessionFails() {
        when(refreshTokenRepository.existsByJtiAndUserCodeAndDeleted(JTI, USER_CODE, false)).thenReturn(false);

        assertError(() -> userImplementation.logout(USER_CODE, JTI, true), ApplicationEnums.INVALID_REFRESH_TOKEN);
        verify(refreshTokenRepository, never()).revokeAllForUser(anyString(), any());
    }

    private void givenActiveUser() {
        when(userRepository.findByEmailAndStatusAndDeleted(EMAIL, UserStatusEnums.ACTIVE, false))
                .thenReturn(Optional.of(user));
    }

    /**
     * The saved session row and the signed refresh token must carry the same
     * jti, or the token could never be refreshed.
     */
    private void assertSessionStartedWithSameJti() {
        ArgumentCaptor<RefreshTokenEntity> saved = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        ArgumentCaptor<String> signedJti = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenRepository).save(saved.capture());
        verify(jwtTokenProvider).generateRefreshToken(eq(USER_CODE), signedJti.capture(), any());
        assertThat(saved.getValue().getJti()).isNotBlank().isEqualTo(signedJti.getValue());
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call,
            ApplicationEnums expected) {
        assertThatThrownBy(call).isInstanceOf(ApplicationException.class)
                .extracting(e -> ((ApplicationException) e).getErrorCode()).isEqualTo(expected.getCode());
    }

    private static RefreshTokenEntity session(String jti) {
        RefreshTokenEntity session = new RefreshTokenEntity();
        session.setJti(jti);
        session.setUserCode(USER_CODE);
        return session;
    }

    private static UserMasterRequest registerRequest() {
        UserMasterRequest request = new UserMasterRequest();
        request.setName("Test");
        request.setEmail(EMAIL);
        request.setPassword("secret123");
        return request;
    }

    private static UserLoginRequest loginRequest(String password) {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(password);
        return request;
    }
}
