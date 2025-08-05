package com.gordan.luckydraw.enums;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomError {
    ACTIVITY_NOT_FOUND("Activity not found", HttpStatus.NOT_FOUND),
    PRIZE_NOT_FOUND("Prize not found", HttpStatus.NOT_FOUND),
    USER_NOT_AUTHORIZED("User not authorized", HttpStatus.FORBIDDEN),
    INVALID_DRAW_REQUEST("Invalid draw request", HttpStatus.BAD_REQUEST),
    PROBABILITY_EXCEED("Probability exceed", HttpStatus.BAD_REQUEST),
    MAX_DRAWS_EXCEEDED("Maximum draws exceeded", HttpStatus.BAD_REQUEST),
    USER_IS_DRAWING("User is drawing", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS("Username already exists", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("Email already exists", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND("Role not found", HttpStatus.NOT_FOUND),
    PRIZE_OUT_OF_STOCK("Prize out of stock", HttpStatus.BAD_REQUEST),
    ;

    private final String message;
    private final HttpStatus status;
}
