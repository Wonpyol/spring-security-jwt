package com.study;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@RequiredArgsConstructor
public class AuthenticationFilter extends GenericFilter {
    private final TokenProvider tokenProvider;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = findBearerToken((HttpServletRequest) request);
        if (token == null) return;
        if (tokenProvider.validateToken(token)) return;

        saveSecurityContext(token);

        chain.doFilter(request, response);

    }
    private void saveSecurityContext(String token) {
        Authentication authentication = tokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Request Header 에서 토큰 정보 추출
    private String findBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(!StringUtils.hasText(bearerToken)) return null;
        if(!bearerToken.startsWith("Bearer")) return null;

        return bearerToken.substring(7);
    }
}
