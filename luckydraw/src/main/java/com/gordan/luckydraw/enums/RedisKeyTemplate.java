package com.gordan.luckydraw.enums;

public enum RedisKeyTemplate {
    USER_DRAW_LOCK_KEY("DRAW_LOCK_%d_%s"),
    USER_DRAW_COUNT_KEY("ACT_%d_USER_DRAW_COUNT"),
    PRIZE_STOCK_KEY("ACT_%d_PRIZE_STOCK");

    private final String template;

    RedisKeyTemplate(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        return String.format(template, args);
    }

    public String getTemplate() {
        return template;
    }
}
