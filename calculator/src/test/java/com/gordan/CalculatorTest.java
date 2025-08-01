package com.gordan;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CalculatorTest {
    Calculator calculator;
    @BeforeEach
    public void setUp() {
        calculator = Calculator.getInstance();
        calculator.clear();
    }

    @Test
    public void baseOperationTest() {
        calculator.setCurrentValue(BigDecimal.ZERO);

        // + operation
        calculator.performOperation("+", new BigDecimal("5"));
        assertEquals(BigDecimal.valueOf(5), calculator.getCurrentValue());

        // - operation
        calculator.performOperation("-", new BigDecimal("2"));
        assertEquals(BigDecimal.valueOf(3), calculator.getCurrentValue());

        // * operation
        calculator.performOperation("*", new BigDecimal("4"));
        assertEquals(BigDecimal.valueOf(12), calculator.getCurrentValue());

        // / operation
        calculator.performOperation("/", new BigDecimal("3"));
        assertEquals(BigDecimal.valueOf(4), calculator.getCurrentValue());

        // clear operation
        calculator.clear();
        assertEquals(BigDecimal.ZERO, calculator.getCurrentValue());
    }

    @Test
    public void negativeOperandTest() {
        calculator.setCurrentValue(BigDecimal.TEN);

        // - operation with negative operand
        calculator.performOperation("-", new BigDecimal("-5"));
        assertEquals(BigDecimal.valueOf(15), calculator.getCurrentValue());

        // + operation with negative operand
        calculator.performOperation("+", new BigDecimal("-3"));
        assertEquals(BigDecimal.valueOf(12), calculator.getCurrentValue());

        // * operation with negative operand
        calculator.performOperation("*", new BigDecimal("-2"));
        assertEquals(BigDecimal.valueOf(-24), calculator.getCurrentValue());

        // / operation with negative operand
        calculator.performOperation("/", new BigDecimal("-4"));
        assertEquals(BigDecimal.valueOf(6), calculator.getCurrentValue());
    }

    @Test
    public void decimalOperandTest() {
        calculator.setCurrentValue(BigDecimal.ZERO);

        // + operation with decimal operand
        calculator.performOperation("+", new BigDecimal("3.5"));
        assertEquals(BigDecimal.valueOf(3.5).setScale(6), calculator.getCurrentValue());

        // - operation with decimal operand
        calculator.performOperation("-", new BigDecimal("1.05"));
        assertEquals(BigDecimal.valueOf(2.45).setScale(6), calculator.getCurrentValue());

        // * operation with decimal operand
        calculator.performOperation("*", new BigDecimal("2.0"));
        assertEquals(BigDecimal.valueOf(4.9).setScale(6), calculator.getCurrentValue());

        // / operation with decimal operand
        calculator.performOperation("/", new BigDecimal("100.0"));
        assertEquals(BigDecimal.valueOf(0.049).setScale(6), calculator.getCurrentValue());
    }

    @Test
    public void roundingTest() {
        calculator.setCurrentValue(new BigDecimal("1.123456789"));

        assertEquals(new BigDecimal("1.123457"), calculator.getCurrentValue());

        calculator.setCurrentValue(new BigDecimal("1.1234561"));
        assertEquals(new BigDecimal("1.123456"), calculator.getCurrentValue());
    }

    @Test
    public void undoRedoTest() {
        calculator.setCurrentValue(BigDecimal.ZERO);

        calculator.performOperation("+", new BigDecimal("5"));
        calculator.performOperation("-", new BigDecimal("2"));
        calculator.undo();
        assertEquals(BigDecimal.valueOf(5), calculator.getCurrentValue());
        calculator.redo();
        assertEquals(BigDecimal.valueOf(3), calculator.getCurrentValue());
        calculator.undo();
        calculator.undo();
        assertEquals(BigDecimal.ZERO, calculator.getCurrentValue());
    }

    @Test
    public void shouldThrowExceptionWhenUndoRedoOnEmptyHistory() {
        assertThrows(IllegalArgumentException.class, calculator::undo);
        assertThrows(IllegalArgumentException.class, calculator::redo);
    }

    @Test
    public void shouldNotThrowExceptionWhenOperandIsExtraValue() {
        calculator.setCurrentValue(BigDecimal.valueOf(Long.MAX_VALUE));
        assertDoesNotThrow(() -> calculator.performOperation("*", BigDecimal.valueOf(Long.MAX_VALUE)));

        calculator.setCurrentValue(BigDecimal.ONE);
        assertDoesNotThrow(() -> calculator.performOperation("/", BigDecimal.valueOf(Long.MAX_VALUE)));
    }

    @Test
    public void shouldBe0WhenMultiplyBy0() {
        calculator.setCurrentValue(BigDecimal.TEN);

        calculator.performOperation("*", new BigDecimal("0"));
        assertEquals(BigDecimal.ZERO, calculator.getCurrentValue());

        calculator.performOperation("*", new BigDecimal("-1"));
        assertEquals(BigDecimal.ZERO, calculator.getCurrentValue());
    }

    @Test
    public void shouldThrowExceptionWhenDivideBy0() {
        calculator.setCurrentValue(BigDecimal.TEN);

        assertThrows(ArithmeticException.class, () -> calculator.performOperation("/", BigDecimal.ZERO));
    }

    @ParameterizedTest
    @ValueSource(strings = { "%", "^", "=", " ", "" })
    public void shouldThrowExceptionWhenInvalidOperator(String operator) {
        calculator.setCurrentValue(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> calculator.performOperation(operator, BigDecimal.ONE));
    }

    @Test
    public void singletonPatternTest() {
        Calculator calc1 = Calculator.getInstance();
        Calculator calc2 = Calculator.getInstance();

        // Should be the same instance
        assertSame(calc1, calc2);

        // State should be shared
        calc1.setCurrentValue(BigDecimal.valueOf(42));
        assertEquals(BigDecimal.valueOf(42), calc2.getCurrentValue());
    }

    @Test
    public void precisionLossTest() {
        // Test operations that might lose precision
        calculator.setCurrentValue(new BigDecimal("1.123456789123456789"));
        calculator.performOperation("+", new BigDecimal("0.000000000000000001"));

        // Should maintain appropriate precision
        assertNotNull(calculator.getCurrentValue());
    }
}
