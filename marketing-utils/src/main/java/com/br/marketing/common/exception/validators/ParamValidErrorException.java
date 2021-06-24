package com.br.marketing.common.exception.validators;

/**
 * 参数异常
 */
public class ParamValidErrorException extends RuntimeException {
    public ParamValidErrorException(String message) {
        super(message);
    }

    public ParamValidErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
