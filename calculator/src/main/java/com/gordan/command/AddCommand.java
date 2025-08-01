package com.gordan.command;

import java.math.BigDecimal;

public class AddCommand extends AbstractCommand {

    public AddCommand(BigDecimal operand) {
        super(operand);
    }

    @Override
    public void execute() {
        getCalculator().setCurrentValue(previousValue.add(operand));
    }

}
