package com.gordan.luckydraw.service.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.gordan.luckydraw.exception.PrizeOutOfStockException;
import com.gordan.luckydraw.model.Prize;
import com.gordan.luckydraw.repository.PrizeRepository;

class PrizeStockHandlerTest {
    private PrizeStockHandler handler;
    private PrizeRepository prizeRepository;
    private RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, Object, Object> hashOperations;

    @BeforeEach
    void setUp() {
        prizeRepository = Mockito.mock(PrizeRepository.class);
        redisTemplate = Mockito.mock(RedisTemplate.class);
        hashOperations = Mockito.mock(HashOperations.class);
        handler = new PrizeStockHandler();
        // 注入 mock
        try {
            java.lang.reflect.Field repoField = PrizeStockHandler.class.getDeclaredField("prizeRepository");
            repoField.setAccessible(true);
            repoField.set(handler, prizeRepository);
            java.lang.reflect.Field redisField = PrizeStockHandler.class.getDeclaredField("redisTemplate");
            redisField.setAccessible(true);
            redisField.set(handler, redisTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Mockito.when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void testGetPrizeStocksWithInit() {
        Prize prize = new Prize();
        prize.setId(1L);
        prize.setStock(10);
        List<Prize> prizes = Collections.singletonList(prize);
        Mockito.when(prizeRepository.findByActivityId(1L)).thenReturn(prizes);
        Mockito.when(hashOperations.get(anyString(), anyString())).thenReturn(null);
        Mockito.doNothing().when(hashOperations).put(anyString(), anyString(), Mockito.anyInt());
        Map<Prize, Integer> stocks = handler.getPrizeStocks(1L);
        assertEquals(1, stocks.size());
        assertEquals(10, stocks.get(prize));
    }

    @Test
    void testGetAvailablePrizes() {
        Prize prize1 = new Prize();
        prize1.setId(1L);
        Prize prize2 = new Prize();
        prize2.setId(2L);
        Map<Prize, Integer> stockMap = new HashMap<>();
        stockMap.put(prize1, 5);
        stockMap.put(prize2, 0);
        List<Prize> available = handler.getAvailablePrizes(stockMap);
        assertEquals(1, available.size());
        assertEquals(prize1, available.get(0));
    }

    @Test
    void testDecrementPrizeStockIfAvailableNormal() {
        Prize prize = new Prize();
        prize.setId(1L);
        Mockito.when(hashOperations.increment(Mockito.anyString(), Mockito.anyString(), Mockito.eq(-1L))).thenReturn(5L);
        assertDoesNotThrow(() -> handler.decrementPrizeStockIfAvailable(1L, prize));
    }

    @Test
    void testDecrementPrizeStockIfAvailableOutOfStock() {
        Prize prize = new Prize();
        prize.setId(1L);
        Mockito.when(hashOperations.increment(Mockito.anyString(), Mockito.anyString(), Mockito.eq(-1L))).thenReturn(-1L);
        Mockito.when(hashOperations.increment(Mockito.anyString(), Mockito.anyString(), Mockito.eq(1L))).thenReturn(0L);
        assertThrows(PrizeOutOfStockException.class, () -> handler.decrementPrizeStockIfAvailable(1L, prize));
    }
}
