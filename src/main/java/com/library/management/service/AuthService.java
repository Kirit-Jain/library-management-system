package com.library.management.service;

import com.library.management.dto.request.LoginRequest;
import com.library.management.dto.request.RegisterRequest;
import com.library.management.dto.response.JwtResponse;
import com.library.management.entity.RefreshToken;
import com.library.management.entity.Role;
import com.library.management.entity.User;
import com.library.management.enums.RoleName;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.DuplicateResourceException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.RefreshTokenRepository;
import com.library.management.repository.RoleRepository;
import com.library.management.repository.UserRepository;
import com.library.management.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                "Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                "Username already taken: " + request.getUsername());
        }

        Role memberRole = roleRepository.findByName(RoleName.MEMBER)
            .orElseThrow(() -> new ResourceNotFoundException("Role MEMBER not found"));

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phone(request.getPhone())
            .address(request.getAddress())
            .roles(Set.of(memberRole))
            .isActive(true)
            .isLocked(false)
            .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        try {
            notificationService.sendWelcomeNotification(savedUser);
        } catch (Exception e) {
            log.error("Failed to send welcome notification to {}", savedUser.getEmail(), e);
        }

        String accessToken = jwtTokenProvider.generateToken(savedUser.getEmail());
        String refreshToken = createRefreshToken(savedUser);

        return buildJwtResponse(savedUser, accessToken, refreshToken);
    }

    @Transactional
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        refreshTokenRepository.deleteByUserId(user.getId());

        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = createRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());
        return buildJwtResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public JwtResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
            .orElseThrow(() -> new BadRequestException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadRequestException("Refresh token expired. Please login again.");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateToken(user.getEmail());

        return buildJwtResponse(user, newAccessToken, refreshTokenValue);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("User logged out: userId={}", userId);
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .expiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
            .createdAt(LocalDateTime.now())
            .build();
        return refreshTokenRepository.save(refreshToken).getToken();
    }

    private JwtResponse buildJwtResponse(User user, String accessToken, String refreshToken) {
        String role = user.getRoles().stream()
            .findFirst()
            .map(r -> r.getName().name())
            .orElse("MEMBER");

        return JwtResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(86400000L)
            .user(JwtResponse.UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(role)
                .build())
            .build();
    }
}