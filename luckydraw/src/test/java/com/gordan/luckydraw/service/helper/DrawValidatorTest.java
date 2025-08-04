package com.gordan.luckydraw.service.helper;

import com.gordan.luckydraw.exception.AppException;
import com.gordan.luckydraw.model.Activity;
import com.gordan.luckydraw.model.Prize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
}
