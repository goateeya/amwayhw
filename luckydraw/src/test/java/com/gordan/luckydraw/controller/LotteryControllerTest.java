package com.gordan.luckydraw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fppt.jedismock.RedisServer;
import com.gordan.luckydraw.model.Activity;
import com.gordan.luckydraw.model.Prize;
import com.gordan.luckydraw.model.payload.LoginRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LotteryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static String adminToken;
    private static String userToken;
    // Remove shared activityId; each test will create its own activity

    @BeforeAll
    static void setUp(@Autowired JdbcTemplate jdbcTemplate, @Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        RedisServer.newRedisServer(16379).start();
        String sqlFile = "src/test/resources/test-admin-init.sql";
        String sql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(sqlFile)));
        for (String statement : sql.split(";")) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                jdbcTemplate.execute(trimmed);
            }
        }
        // Admin login
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setUsername("admin");
        adminLogin.setPassword("123");
        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString()).get("token").asText();
        // User login
        LoginRequest userLogin = new LoginRequest();
        userLogin.setUsername("min001");
        userLogin.setPassword("000000");
        MvcResult userResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn();
        userToken = objectMapper.readTree(userResult.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void testAdminCreateActivity() throws Exception {
        Activity activity = new Activity();
        activity.setName("Test Activity " + System.nanoTime());
        activity.setMaxNumberOfDrawsPerUser(10);
        mockMvc.perform(post("/api/lottery/activity")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activity)))
                .andExpect(status().isOk());
    }

    @Test
    void testAdminSetPrizes() throws Exception {
        // Create new activity for this test
        Activity activity = new Activity();
        activity.setName("Test Activity " + System.nanoTime());
        activity.setMaxNumberOfDrawsPerUser(10);
        MvcResult result = mockMvc.perform(post("/api/lottery/activity")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activity)))
                .andExpect(status().isOk())
                .andReturn();
        Long activityId = objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("id").asLong();
        Prize prize = new Prize();
        prize.setName("Test Prize");
        prize.setStock(10);
        prize.setProbability(0.5);
        mockMvc.perform(post("/api/lottery/prizes/" + activityId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singletonList(prize)))
        ).andExpect(status().isOk());
    }

    @Test
    void testUserDraw() throws Exception {
        // Create new activity for this test
        Activity activity = new Activity();
        activity.setName("Test Activity " + System.nanoTime());
        activity.setMaxNumberOfDrawsPerUser(10);
        MvcResult result = mockMvc.perform(post("/api/lottery/activity")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activity)))
                .andExpect(status().isOk())
                .andReturn();
        Long activityId = objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("id").asLong();
        Prize prize = new Prize();
        prize.setName("Test Prize");
        prize.setStock(10);
        prize.setProbability(0.5);
        mockMvc.perform(post("/api/lottery/prizes/" + activityId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singletonList(prize)))
        ).andExpect(status().isOk());
        mockMvc.perform(post("/api/lottery/draw/" + activityId)
                .header("Authorization", "Bearer " + userToken)
                .param("times", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void testUserCreateActivityForbidden() throws Exception {
        Activity activity = new Activity();
        activity.setName("User Activity");
        activity.setMaxNumberOfDrawsPerUser(5);
        mockMvc.perform(post("/api/lottery/activity")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activity)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUserSetPrizesForbidden() throws Exception {
        // Create new activity for this test
        Activity activity = new Activity();
        activity.setName("User Activity " + System.nanoTime());
        activity.setMaxNumberOfDrawsPerUser(5);
        MvcResult result = mockMvc.perform(post("/api/lottery/activity")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activity)))
                .andExpect(status().isOk())
                .andReturn();
        Long activityId = objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("id").asLong();
        Prize prize = new Prize();
        prize.setName("User Prize");
        prize.setStock(5);
        prize.setProbability(0.5);
        mockMvc.perform(post("/api/lottery/prizes/" + activityId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singletonList(prize)))
        ).andExpect(status().isForbidden());
    }

    @Test
    void testAdminDrawForbidden() throws Exception {
        // Create new activity for this test
        Activity activity = new Activity();
        activity.setName("Test Activity " + System.nanoTime());
        activity.setMaxNumberOfDrawsPerUser(10);
        MvcResult result = mockMvc.perform(post("/api/lottery/activity")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activity)))
                .andExpect(status().isOk())
                .andReturn();
        Long activityId = objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("id").asLong();
        Prize prize = new Prize();
        prize.setName("Test Prize");
        prize.setStock(10);
        prize.setProbability(0.5);
        mockMvc.perform(post("/api/lottery/prizes/" + activityId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singletonList(prize)))
        ).andExpect(status().isOk());
        mockMvc.perform(post("/api/lottery/draw/" + activityId)
                .header("Authorization", "Bearer " + adminToken)
                .param("times", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDrawWithInvalidTokenUnauthorized() throws Exception {
        // Create new activity for this test
        Activity activity = new Activity();
        activity.setName("Test Activity " + System.nanoTime());
        activity.setMaxNumberOfDrawsPerUser(10);
        MvcResult result = mockMvc.perform(post("/api/lottery/activity")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activity)))
                .andExpect(status().isOk())
                .andReturn();
        Long activityId = objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("id").asLong();
        Prize prize = new Prize();
        prize.setName("Test Prize");
        prize.setStock(10);
        prize.setProbability(0.5);
        mockMvc.perform(post("/api/lottery/prizes/" + activityId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singletonList(prize)))
        ).andExpect(status().isOk());
        mockMvc.perform(post("/api/lottery/draw/" + activityId)
                .header("Authorization", "Bearer invalidtoken")
                .param("times", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDrawWithInvalidActivityId() throws Exception {
        mockMvc.perform(post("/api/lottery/draw/99999")
                .header("Authorization", "Bearer " + userToken)
                .param("times", "1"))
                .andExpect(status().is4xxClientError());
    }
}
