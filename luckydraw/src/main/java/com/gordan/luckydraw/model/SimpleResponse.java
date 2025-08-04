package com.gordan.luckydraw.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleResponse<T> {
    private String message;
    private T data;

    public SimpleResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }
}
