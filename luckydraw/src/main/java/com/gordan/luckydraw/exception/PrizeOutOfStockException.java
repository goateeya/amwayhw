package com.gordan.luckydraw.exception;

public class PrizeOutOfStockException extends RuntimeException {
    public PrizeOutOfStockException() {
        super("Prize is out of stock");
    }
}
