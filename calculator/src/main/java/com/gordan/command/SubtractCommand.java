package com.gordan.command;

import java.math.BigDecimal;

public class SubtractCommand extends AbstractCommand {

    public SubtractCommand(BigDecimal operand) {
        super(operand);
    }

    @Override
    public void execute() {
        getCalculator().setCurrentValue(previousValue.subtract(operand));
    }
    
}
