package com.gordan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Stack;

import com.gordan.command.AddCommand;
import com.gordan.command.Command;
import com.gordan.command.DivideCommand;
import com.gordan.command.MultiplyCommand;
import com.gordan.command.SubtractCommand;
import com.gordan.enums.Operator;

import lombok.Data;
import lombok.Getter;

@Data
public class Calculator {
    @Getter
    private static final Calculator instance = new Calculator();
    private BigDecimal currentValue;
    private Stack<Command> historyStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    public void performOperation(String operator, BigDecimal operand) {
        Operator op = Operator.fromSymbol(operator);
        Command command = null;
        switch (op) {
            case ADD:
                command = new AddCommand(operand);
                break;
            case SUB:
                command = new SubtractCommand(operand);
                break;
            case MUL:
                command = new MultiplyCommand(operand);
                break;
            case DIV:
                command = new DivideCommand(operand);
                break;
            default:
                throw new IllegalArgumentException("Invalid operator");
        }
        command.execute();
        historyStack.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (!historyStack.isEmpty()) {
            Command command = historyStack.pop();
            command.undo();
            redoStack.push(command);
            return;
        }
        throw new IllegalArgumentException("No operations to undo");
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            historyStack.push(command);
            return;
        }
        throw new IllegalArgumentException("No operations to redo");
    }

    public void clear() {
        currentValue = BigDecimal.ZERO;
        historyStack.clear();
        redoStack.clear();
    }

    public BigDecimal getCurrentValue() {
        BigDecimal result = currentValue.setScale(6, RoundingMode.HALF_UP);
        if (result.stripTrailingZeros().scale() <= 0) {
            return result.setScale(0, RoundingMode.HALF_UP);
        }
        return result;
    }
}
