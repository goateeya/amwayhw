package com.gordan.luckydraw.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.gordan.luckydraw.model.MessageResponse;

@ControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(new MessageResponse(ex.getMessage()));
    }
}
