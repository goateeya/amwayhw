package com.gordan.luckydraw.service.helper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.gordan.luckydraw.enums.CustomError;
import com.gordan.luckydraw.exception.AppException;
import com.gordan.luckydraw.model.Activity;
import com.gordan.luckydraw.model.Prize;

class DrawValidatorTest {
    private DrawValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DrawValidator();
    }

    @Test
    void testValidateMaxDrawExceededThrows() {
        Activity activity = new Activity();
        activity.setMaxNumberOfDrawsPerUser(5);
        assertThrows(AppException.class, () -> validator.validateMaxDrawExceeded(activity, 4, 2));
    }

    @Test
    void testValidateMaxDrawExceededNoException() {
        Activity activity = new Activity();
        activity.setMaxNumberOfDrawsPerUser(5);
        assertDoesNotThrow(() -> validator.validateMaxDrawExceeded(activity, 2, 2));
    }

    @Test
    void testValidatePrizeStockThrows() {
        List<Prize> emptyList = new ArrayList<>();
        assertThrows(AppException.class, () -> validator.validatePrizeStock(emptyList));
    }

    @Test
    void testValidatePrizeStockNoException() {
        List<Prize> list = new ArrayList<>();
        Prize prize = new Prize();
        list.add(prize);
        assertDoesNotThrow(() -> validator.validatePrizeStock(list));
    }

    @Test
    void testValidateProbabilityExceedThrows() {
        assertThrows(AppException.class, () -> validator.validateProbabilityExceed(1.1));
    }

    @Test
    void testValidateProbabilityExceedNoException() {
        assertDoesNotThrow(() -> validator.validateProbabilityExceed(0.9));
    }

    @ParameterizedTest
    @ValueSource(ints = { -5, -1, 0 })
    @DisplayName("validateDrawTimes throws for negative and zero values")
    void testValidateDrawTimesThrows(int drawTimes) {
        AppException ex = assertThrows(AppException.class, () -> new DrawValidator().validateDrawTimes(drawTimes));
        assertEquals(CustomError.INVALID_DRAW_REQUEST, ex.getCustomError());
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 5, 100 })
    @DisplayName("validateDrawTimes does not throw for positive values")
    void testValidateDrawTimesPass(int drawTimes) {
        assertDoesNotThrow(() -> new DrawValidator().validateDrawTimes(drawTimes));
    }
}
