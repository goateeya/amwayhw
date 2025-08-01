package com.gordan.command;

import java.math.BigDecimal;

import com.gordan.Calculator;

import lombok.Getter;

public abstract class AbstractCommand implements Command {
    @Getter
    private final Calculator calculator;
    protected final BigDecimal operand;
    protected final BigDecimal previousValue;

    public AbstractCommand(BigDecimal operand) {
        this.calculator = Calculator.getInstance();
        this.operand = operand;
        this.previousValue = calculator.getCurrentValue();
    }

    @Override
    public void execute() {
        // Default implementation can be empty or provide common functionality
    }

    @Override
    public void undo() {
        getCalculator().setCurrentValue(previousValue);
    }
}
