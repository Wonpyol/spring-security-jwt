package com.study.controller;

import com.study.dto.MemberLoginRequestDto;
import com.study.dto.TokenInfoDto;
import com.study.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {
    private final MemberService memberService;

    @PostMapping("/token")
    public TokenInfoDto login(@RequestBody MemberLoginRequestDto data) {
        System.out.println("login");
        return memberService.login(data.getMemberId(), data.getPassword());
    }

    @PostMapping("/test")
    public String test() {
        System.out.println("test");
        return "Success";
    }

}
