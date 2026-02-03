package com.example.bankcards.service.impl;

import com.example.bankcards.dto.requests.AuthRequest;
import com.example.bankcards.dto.requests.RegisterAdminRequest;
import com.example.bankcards.dto.response.AuthResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.admin.registration.code:ADMIN_SECRET_2024}")
    private String adminRegistrationCode;
    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public void register(AuthRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,"USERNAME_ALREADY_EXISTS",
                    "Пользователь с таким именем уже существует"
            );
        }

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,"PHONE_ALREADY_EXISTS",
                    "Пользователь с таким номером телефона уже существует"
            );
        }

        User user = new User();
        user.setPhoneNumber(request.phoneNumber());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        userRepository.save(user);
    }
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.username());

        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token);
    }

    @Override
    public Long getUserIdFromAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Пользователь не аутентифицирован");
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Пользователь не найден"));
    }

    @Override
    @Transactional
    public void registerAdmin(RegisterAdminRequest request) {

        if (!adminRegistrationCode.equals(request.getAdminCode())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_ADMIN_CODE",
                    "Неверный код администратора"
            );
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "USERNAME_ALREADY_EXISTS",
                    "Пользователь с таким именем уже существует"
            );
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "PHONE_ALREADY_EXISTS",
                    "Пользователь с таким номером телефона уже существует"
            );
        }

        User admin = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
    }

}
