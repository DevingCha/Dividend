package com.example.stock.exception.impl;

import com.example.stock.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NoTickerValueException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "종목코드 값이 옳바르지 않습니다.";
    }
}
