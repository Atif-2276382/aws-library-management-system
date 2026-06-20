package com.library.service;

import com.library.dto.AuthDtos;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.MemberRepository;
import com.library.repository.UserRepository;
import com.library.security.JwtService;
import com.library.security.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @InjectMocks
    private AuthService authService;

    @Test
    void register_duplicateUsername_throws() {
        AuthDtos.RegisterRequest request =
                new AuthDtos.RegisterRequest("john", "password", Role.MEMBER, "John", "M001");
        when(userRepository.existsByUsername("john")).thenReturn(true);
        assertThrows(BusinessException.class, () -> authService.register(request));
    }
}
