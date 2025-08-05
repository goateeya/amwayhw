package com.gordan.luckydraw.model.payload;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleResponse<T> extends MessageResponse{
    private T data;

    public SimpleResponse(String message, T data) {
        super(message);
        this.data = data;
    }
}
