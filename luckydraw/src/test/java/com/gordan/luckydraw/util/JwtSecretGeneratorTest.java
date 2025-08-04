package com.gordan.luckydraw.util;

import org.junit.jupiter.api.Test;
import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class JwtSecretGeneratorTest {
    @Test
    void testGenerateSecureJwtSecret() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        String base64Key = Base64.getEncoder().encodeToString(key);
        System.out.println("Generated JWT Secret (base64, 32 bytes): " + base64Key);
        assertNotNull(base64Key);
        assertTrue(Base64.getDecoder().decode(base64Key).length >= 32);
    }
}
