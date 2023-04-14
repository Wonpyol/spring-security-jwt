package com.study;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

@SpringBootTest
class TokenProviderTest {
  @Autowired
  private TokenProvider tokenProvider;

  @Test
  void generateToken() {
    tokenProvider.generateToken(null);
  }

  @Test
  void refreshToken() {
    String refreshToken = tokenProvider.createRefreshToken();
    Assertions.assertNotNull(refreshToken);
  }



}