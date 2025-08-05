package com.gordan.luckydraw.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gordan.luckydraw.model.payload.LoginRequest;
import com.gordan.luckydraw.model.payload.SignupRequest;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void setupValidUser(@Autowired JdbcTemplate jdbcTemplate) throws Exception {
        String sqlFile = "src/test/resources/test-admin-init.sql";
        String sql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(sqlFile)));
        for (String statement : sql.split(";")) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                jdbcTemplate.execute(trimmed);
            }
        }
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRegisterUserForbidden() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("testuser@example.com");
        signupRequest.setPassword("password");
        signupRequest.setRole(Collections.singleton("user"));
        mockMvc.perform(post("/api/auth/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRegisterUserSuccess() throws Exception {
        // 1. 登入 admin 取得 JWT token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("123");
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();
        String token = com.fasterxml.jackson.databind.JsonNode.class.cast(
                objectMapper.readTree(response)).get("token").asText();

        // 2. 註冊 user role 帳號
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("newpassword");
        signupRequest.setRole(Collections.singleton("user"));
        mockMvc.perform(post("/api/auth/user")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());
    }

    
}
