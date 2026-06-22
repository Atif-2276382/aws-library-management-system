package com.library.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LibraryUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService,
            LibraryUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (!tokenBlacklistService.isBlacklisted(token)) {
                try {
                    Claims claims = jwtService.parseClaims(token);
                    String username = claims.getSubject();
                    LibraryUserDetails userDetails =
                            (LibraryUserDetails) userDetailsService.loadUserByUsername(username);
                    String role = claims.get("role", String.class);
                    var auth = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("Authenticated request {} for user={}", request.getRequestURI(), username);
                } catch (Exception ex) {
                    log.warn("Failed to authenticate token for request {}: {}", request.getRequestURI(), ex.getMessage());
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.warn("Rejected blacklisted token for request {}", request.getRequestURI());
            }
        } else {
            log.debug("No bearer token found for request {}", request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }
}
