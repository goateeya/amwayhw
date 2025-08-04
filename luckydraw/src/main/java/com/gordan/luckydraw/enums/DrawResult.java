package com.gordan.luckydraw.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DrawResult {
    NO_PRIZE("銘謝惠顧"),
    OUT_OF_STOCK("獎品已抽完, 請稍後再試");

    private final String label;
}
