package com.study;

import com.study.dto.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TokenProvider {
  private Key key;

  @Value("${jwt.secret}")
  private String secretKey;

  @PostConstruct
  public void init() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * 토큰 생성
   */
  public TokenInfo generateToken(Authentication authentication) {
    //권한 가져오기
    String authorities = getAuthorities(authentication);
    //엑세스 토큰 생성
    String accessToken = createAccessToken(authentication, authorities);
    //리프레쉬 토큰 생성
    String refreshToken = createRefreshToken();

    return TokenInfo.builder()
        .grantType("Bearer")
        .accessToken(accessToken)
        .refreshToken(refreshToken).build();
  }

  protected String createRefreshToken() {
    return Jwts.builder()
        .setExpiration(getAccessTokenExpireDate())
        .signWith(this.key, SignatureAlgorithm.HS256)
        .compact();
  }

  private String createAccessToken(Authentication authentication, String authorities) {
    return Jwts.builder()
        .setSubject(authentication.getName())
        .claim("auth", authorities)
        .setExpiration(getAccessTokenExpireDate())
        .signWith(this.key, SignatureAlgorithm.HS256)
        .compact();
  }

  private Date getAccessTokenExpireDate() {
    long now = (new Date()).getTime();
    return new Date(now + 86400000);
  }

  private String getAuthorities(Authentication authentication) {
    return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));
  }

  public Authentication getAuthentication(String accessToken) {
    // 토큰 복호화
    Claims claims = tokenDecryption(accessToken);
    if (claims.get("auth") == null) throw new RuntimeException("권한 정보가 없는 토큰입니다.");
    // 클레임에서 권한 정보 가져오기
    Collection<? extends GrantedAuthority> authorities = getClaimsAuthorities(claims);
    // UserDetails 객체를 만들어서 Authentication 리턴
    return createAuthenticationToken(claims.getSubject(), authorities);
  }

  private UsernamePasswordAuthenticationToken createAuthenticationToken(String subject, Collection<? extends GrantedAuthority> authorities) {
    UserDetails principal = new User(subject, "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, "", authorities);
  }


  private  Collection<? extends GrantedAuthority> getClaimsAuthorities(Claims claims) {
    return Arrays.stream(claims.get("auth").toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
  }

  // 토큰 정보를 검증하는 메서드
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(this.key).build().parseClaimsJws(token);
      return true;
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      log.info("Invalid JWT Token", e);
    } catch (ExpiredJwtException e) {
      log.info("Expired JWT Token", e);
    } catch (UnsupportedJwtException e) {
      log.info("Unsupported JWT Token", e);
    } catch (IllegalArgumentException e) {
      log.info("JWT claims string is empty.", e);
    }
    return false;
  }

  private Claims tokenDecryption(String accessToken) {
    try {
      return Jwts.parserBuilder().setSigningKey(this.key).build().parseClaimsJws(accessToken).getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }
}
