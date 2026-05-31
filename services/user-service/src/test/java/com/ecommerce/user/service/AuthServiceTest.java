package com.ecommerce.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.user.domain.User;
import com.ecommerce.user.domain.UserStatus;
import com.ecommerce.user.dto.AuthResponse;
import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.RegisterRequest;
import com.ecommerce.user.exception.AccountLockedException;
import com.ecommerce.user.exception.EmailAlreadyExistsException;
import com.ecommerce.user.exception.InvalidCredentialsException;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.security.JwtTokenProvider;
import com.ecommerce.user.security.RefreshTokenService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "maxFailedAttempts", 3);
        ReflectionTestUtils.setField(authService, "lockDurationMinutes", 15L);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("dup@test.com", "password123", "User")))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void loginLocksAccountAfterRepeatedFailures() {
        User user = activeUser();
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginRequest request = new LoginRequest("user@test.com", "wrong");

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository, org.mockito.Mockito.atLeast(3)).save(any(User.class));
    }

    @Test
    void loginRejectsLockedAccount() {
        User user = activeUser();
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(Instant.now().plusSeconds(600));
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@test.com", "password123")))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    void loginResetsFailedAttemptsOnSuccess() {
        User user = activeUser();
        user.setFailedLoginCount(2);
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(anyLong(), anyString())).thenReturn("access");
        when(refreshTokenService.issueRefreshToken(anyLong())).thenReturn("refresh");
        when(jwtTokenProvider.getAccessTokenTtlSeconds()).thenReturn(7200L);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.login(new LoginRequest("user@test.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("access");
        verify(userRepository).save(user);
        assertThat(user.getFailedLoginCount()).isZero();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    private User activeUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setPasswordHash("hash");
        user.setDisplayName("User");
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginCount(0);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }
}
