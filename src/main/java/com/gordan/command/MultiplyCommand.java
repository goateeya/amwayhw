package com.gordan.command;

import java.math.BigDecimal;

public class MultiplyCommand extends AbstractCommand {

    public MultiplyCommand(BigDecimal operand) {
        super(operand);
    }

    @Override
    public void execute() {
        getCalculator().setCurrentValue(previousValue.multiply(operand));
    }
}
