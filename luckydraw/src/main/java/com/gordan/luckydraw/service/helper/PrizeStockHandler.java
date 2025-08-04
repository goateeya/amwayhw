package com.gordan.luckydraw.service.helper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.gordan.luckydraw.enums.RedisKeyTemplate;
import com.gordan.luckydraw.exception.PrizeOutOfStockException;
import com.gordan.luckydraw.model.Prize;
import com.gordan.luckydraw.repository.PrizeRepository;

@Component
public class PrizeStockHandler {
    @Autowired
    private PrizeRepository prizeRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Map<Prize, Integer> getPrizeStocks(Long activityId) {
        return prizeRepository.findByActivityId(activityId)
                .stream()
                .collect(Collectors.toMap(prize -> prize, prize -> {
                    Integer stock = (Integer) redisTemplate.opsForHash()
                            .get(RedisKeyTemplate.PRIZE_STOCK_KEY.format(activityId), prize.getId().toString());
                    if (stock == null) {
                        return initPrizeStock(activityId, prize);
                    } else {
                        return stock;
                    }
                }));
    }

    public List<Prize> getAvailablePrizes(Map<Prize, Integer> prizeStockMap) {
        return prizeStockMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey).toList();
    }

    public void decrementPrizeStockIfAvailable(Long activityId, Prize prize) {
        String key = RedisKeyTemplate.PRIZE_STOCK_KEY.format(activityId);
        Long stock = redisTemplate.opsForHash().increment(key, prize.getId().toString(), -1);
        if (stock < 0) {
            redisTemplate.opsForHash().increment(key, prize.getId().toString(), 1);
            throw new PrizeOutOfStockException();
        }
    }

    private int initPrizeStock(Long activityId, Prize prize) {
        int stock = prize.getStock();
        redisTemplate.opsForHash().put(RedisKeyTemplate.PRIZE_STOCK_KEY.format(activityId),
                prize.getId().toString(), stock);
        return stock;
    }
}
