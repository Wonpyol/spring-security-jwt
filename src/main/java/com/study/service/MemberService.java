package com.study.service;


import com.study.TokenProvider;
import com.study.dto.TokenInfoDto;
import com.study.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;

    public TokenInfoDto login(String memberId, String password) {
        //id/password 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberId, password);
        //id/password 인증 토큰 검증
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        //jwt 토큰 생성
        return tokenProvider.generateToken(authentication);
    }
}
