package com.gordan.luckydraw.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.extern.log4j.Log4j2;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class BCryptPasswordEncoderTest {
    @Test
    void testEncodePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123";
        String encodedPassword = encoder.encode(rawPassword);
        log.info("Encoded password: " + encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$"));
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }
}
