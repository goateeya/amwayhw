package com.gordan.luckydraw.enums;

public enum CustomError {
    ACTIVITY_NOT_FOUND("Activity not found"),
    PRIZE_NOT_FOUND("Prize not found"),
    USER_NOT_AUTHORIZED("User not authorized"),
    INVALID_DRAW_REQUEST("Invalid draw request"),
    PROBABILITY_EXCEED("Probability exceed"),
    MAX_DRAWS_EXCEEDED("Maximum draws exceeded"),
    USER_IS_DRAWING("User is drawing"),
    ;

    private final String message;

    CustomError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
