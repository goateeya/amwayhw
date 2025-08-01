package com.gordan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Operator {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    ;

    @Getter
    private final String symbol;

    public static Operator fromSymbol(String symbol) {
        if (symbol != null && !symbol.trim().isEmpty()) {
            String target = symbol.trim();
            for (Operator operator : values()) {
                if (operator.getSymbol().equals(target)) {
                    return operator;
                }
            }
        }
        throw new IllegalArgumentException("Unknown operator: " + symbol);
    }
}
