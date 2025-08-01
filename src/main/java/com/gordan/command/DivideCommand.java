package com.gordan.command;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DivideCommand extends AbstractCommand {

    public DivideCommand(BigDecimal operand) {
        super(operand);
    }

    @Override
    public void execute() {
        if (operand.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Division by zero is not allowed.");
        }
        getCalculator().setCurrentValue(previousValue.divide(operand, 6, RoundingMode.HALF_UP));
    }

}
