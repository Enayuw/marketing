package com.br.marketing.exception;

import lombok.Data;

@Data
public class HxResultRuntimeException extends RuntimeException{

    public HxResultRuntimeException() {
        super();
    }

    public HxResultRuntimeException(String message) {
        super(message);
    }
}
