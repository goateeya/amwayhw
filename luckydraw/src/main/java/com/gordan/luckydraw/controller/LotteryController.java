package com.gordan.luckydraw.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gordan.luckydraw.model.Activity;
import com.gordan.luckydraw.model.Prize;
import com.gordan.luckydraw.model.payload.SimpleResponse;
import com.gordan.luckydraw.security.jwt.JwtUtils;
import com.gordan.luckydraw.service.LotteryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lottery")
public class LotteryController {
    @Autowired
    private LotteryService lotteryService;

    @Autowired
    private JwtUtils jwtUtils;

    // 建立活動 API，限 ADMIN 使用
    @PostMapping("/activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SimpleResponse<Activity>> createActivity(
            @Valid @RequestBody Activity activity) {
        return ResponseEntity
                .ok(new SimpleResponse<>("活動建立成功", lotteryService.createActivity(activity)));
    }

    @PostMapping("/prizes/{activityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SimpleResponse<List<Prize>>> setPrizes(@PathVariable Long activityId,
            @Valid @RequestBody List<Prize> prizes) {
        return ResponseEntity.ok(
                new SimpleResponse<>("獎品設定成功", lotteryService.savePrizes(activityId, prizes)));
    }

    // 抽獎 API
    @PostMapping("/draw/{activityId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<String>> draw(@PathVariable Long activityId, @RequestParam int drawTimes,
            @RequestHeader("Authorization") String token) {
        String userId = jwtUtils.getUserNameFromJwtToken(token);
        List<String> results = lotteryService.draw(activityId, userId, drawTimes);
        return ResponseEntity.ok(results);
    }
}
