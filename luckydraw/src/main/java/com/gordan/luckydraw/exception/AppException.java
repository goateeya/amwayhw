package com.gordan.luckydraw.exception;

import org.springframework.http.HttpStatus;

import com.gordan.luckydraw.enums.CustomError;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final CustomError customError;
    private final HttpStatus httpStatus;

    public AppException(CustomError customError) {
        super(customError.getMessage());
        this.customError = customError;
        this.httpStatus = customError.getStatus();
    }
}
