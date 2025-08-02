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

import com.gordan.luckydraw.model.Prize;
import com.gordan.luckydraw.service.LotteryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lottery")
public class LotteryController {
    @Autowired
    private LotteryService lotteryService;
    
    @PostMapping("/prizes/{activityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> setPrizes(@PathVariable Long activityId, @Valid @RequestBody List<Prize> prizes) {
        lotteryService.setPrizes(activityId, prizes);
        return ResponseEntity.ok("獎品設定成功");
    }

    // 抽獎 API
    @PostMapping("/draw/{activityId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<String>> draw(@PathVariable Long activityId, @RequestParam int times, @RequestHeader("Authorization") String token) {
        String userId = extractUserIdFromToken(token); // 自訂方法解析 JWT
        List<String> results = lotteryService.draw(activityId, userId, times);
        return ResponseEntity.ok(results);
    }
}
