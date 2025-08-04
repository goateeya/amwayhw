package com.gordan.luckydraw.service.helper;

import com.gordan.luckydraw.enums.DrawResult;
import com.gordan.luckydraw.exception.PrizeOutOfStockException;
import com.gordan.luckydraw.model.DrawEvent;
import com.gordan.luckydraw.model.Prize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class DrawProcessorTest {
    private DrawProcessor drawProcessor;
    private PrizeStockHandler prizeStockHandler;

    @BeforeEach
    void setUp() {
        prizeStockHandler = Mockito.mock(PrizeStockHandler.class);
        drawProcessor = new DrawProcessor();
        // 使用反射注入 mock PrizeStockHandler
        try {
            java.lang.reflect.Field field = DrawProcessor.class.getDeclaredField("prizeStockHandler");
            field.setAccessible(true);
            field.set(drawProcessor, prizeStockHandler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testDrawPrize() {
        Prize prizeA = new Prize();
        prizeA.setId(1L);
        prizeA.setName("A");
        Prize prizeB = new Prize();
        prizeB.setId(2L);
        prizeB.setName("B");
        Map<Prize, Double> map = new LinkedHashMap<>();
        map.put(prizeA, 0.5);
        map.put(prizeB, 0.7);
        Mockito.doNothing().when(prizeStockHandler).decrementPrizeStockIfAvailable(any(), any());
        List<DrawEvent> result = drawProcessor.performDraws(1L, "user1", 10, map);
        assertEquals(10, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getPrizeName().equals("A") || e.getPrizeName().equals("B") || e.getPrizeName().equals(DrawResult.NO_PRIZE.getLabel())));
    }

    @Test
    void testNoPrize() {
        Prize prizeA = new Prize();
        prizeA.setId(1L);
        prizeA.setName("A");
        Map<Prize, Double> map = new LinkedHashMap<>();
        map.put(prizeA, 0.1); // 機率很低
        Mockito.doNothing().when(prizeStockHandler).decrementPrizeStockIfAvailable(any(), any());
        List<DrawEvent> result = drawProcessor.performDraws(1L, "user2", 10, map);
        assertTrue(result.stream().anyMatch(e -> DrawResult.NO_PRIZE.getLabel().equals(e.getPrizeName())));
    }

    @Test
    void testPrizeOutOfStock() {
        Prize prizeA = new Prize();
        prizeA.setId(1L);
        prizeA.setName("A");
        Map<Prize, Double> map = new LinkedHashMap<>();
        map.put(prizeA, 1.0);
        Mockito.doThrow(new PrizeOutOfStockException()).when(prizeStockHandler).decrementPrizeStockIfAvailable(any(), any());
        List<DrawEvent> result = drawProcessor.performDraws(1L, "user3", 1, map);
        assertEquals(DrawResult.OUT_OF_STOCK.name(), result.get(0).getPrizeName());
    }
}
