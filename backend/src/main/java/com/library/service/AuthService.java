package com.library.service;

import com.library.dto.AuthDtos;
import com.library.entity.Member;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.MemberRepository;
import com.library.repository.UserRepository;
import com.library.security.JwtService;
import com.library.security.LibraryUserDetails;
import com.library.security.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(
            UserRepository userRepository,
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already exists");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user = userRepository.save(user);

        Integer memberId = null;
        if (request.role() == Role.MEMBER) {
            if (!StringUtils.hasText(request.memberName()) || !StringUtils.hasText(request.membershipId())) {
                throw new BusinessException("Member name and membership ID are required for members");
            }
            String membershipId = request.membershipId().trim();
            if (memberRepository.existsByMembershipId(membershipId)) {
                throw new BusinessException("Membership ID already exists");
            }
            Member member = new Member();
            member.setName(request.memberName().trim());
            member.setMembershipId(membershipId);
            member.setUser(user);
            memberId = memberRepository.save(member).getMemberId();
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getUserId());
        log.info("User registered: username={} userId={} role={}", user.getUsername(), user.getUserId(), user.getRole());
        return new AuthDtos.AuthResponse(token, user.getUsername(), user.getRole(), user.getUserId(), memberId);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("User not found"));
        Integer memberId = memberRepository.findByUserUserId(user.getUserId())
                .map(Member::getMemberId)
                .orElse(null);
        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getUserId());
        log.info("User authenticated: username={} userId={} role={}", user.getUsername(), user.getUserId(), user.getRole());
        return new AuthDtos.AuthResponse(token, user.getUsername(), user.getRole(), user.getUserId(), memberId);
    }

    public void logout(String token) {
        tokenBlacklistService.blacklist(token);
        log.info("User logged out and token blacklisted");
    }

    public AuthDtos.AuthResponse currentUser(LibraryUserDetails userDetails) {
        User user = userDetails.getUser();
        Integer memberId = memberRepository.findByUserUserId(user.getUserId())
                .map(Member::getMemberId)
                .orElse(null);
        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getUserId());
        log.debug("Returning current user response for username={}", user.getUsername());
        return new AuthDtos.AuthResponse(token, user.getUsername(), user.getRole(), user.getUserId(), memberId);
    }
}
