package com.gordan.luckydraw.exception;

import com.gordan.luckydraw.enums.CustomError;

import lombok.Getter;

public class AppException extends RuntimeException {
    @Getter
    private final CustomError customError;
    
    public AppException(CustomError customError) {
        super(customError.getMessage());
        this.customError = customError;
    }
}
