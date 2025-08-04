package com.gordan.luckydraw.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum CustomError {
    ACTIVITY_NOT_FOUND("Activity not found", HttpStatus.NOT_FOUND),
    PRIZE_NOT_FOUND("Prize not found", HttpStatus.NOT_FOUND),
    USER_NOT_AUTHORIZED("User not authorized", HttpStatus.FORBIDDEN),
    INVALID_DRAW_REQUEST("Invalid draw request", HttpStatus.BAD_REQUEST),
    PROBABILITY_EXCEED("Probability exceed", HttpStatus.BAD_REQUEST),
    MAX_DRAWS_EXCEEDED("Maximum draws exceeded", HttpStatus.BAD_REQUEST),
    USER_IS_DRAWING("User is drawing", HttpStatus.CONFLICT),
    ;

    private final String message;
    private final HttpStatus status;

    CustomError(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
