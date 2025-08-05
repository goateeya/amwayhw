package com.gordan.luckydraw.security.jwt;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.Test;
// 不需要 SpringBootTest 或 @Value

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

class JwtUtilsTest {
    private final String jwtSecret = "q0ocPAKNHarwPL94gOayj9iOqDDrMxeDBzm11/3/yIQ=";

    @Test
    void testGetUserNameFromJwtToken_withBearerPrefix() {
        // Arrange
        String username = "testuser";
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(jwtSecret)), SignatureAlgorithm.HS256)
                .compact();
        String bearerToken = "Bearer " + token;
        JwtUtils jwtUtils = new JwtUtils();
        // 直接注入 jwtSecret
        java.lang.reflect.Field secretField;
        try {
            secretField = JwtUtils.class.getDeclaredField("jwtSecret");
            secretField.setAccessible(true);
            secretField.set(jwtUtils, jwtSecret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Act
        String result = jwtUtils.getUserNameFromJwtToken(bearerToken);
        // Assert
        assertEquals(username, result);
    }

    @Test
    void testGetUserNameFromJwtToken_withoutBearerPrefix() {
        // Arrange
        String username = "testuser";
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(jwtSecret)), SignatureAlgorithm.HS256)
                .compact();
        JwtUtils jwtUtils = new JwtUtils();
        // 直接注入 jwtSecret
        java.lang.reflect.Field secretField;
        try {
            secretField = JwtUtils.class.getDeclaredField("jwtSecret");
            secretField.setAccessible(true);
            secretField.set(jwtUtils, jwtSecret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Act
        String result = jwtUtils.getUserNameFromJwtToken(token);
        // Assert
        assertEquals(username, result);
    }
}
