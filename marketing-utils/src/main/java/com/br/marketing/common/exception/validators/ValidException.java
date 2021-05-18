package com.br.marketing.common.exception.validators;

/**
 * @author Wang Weiwei
 * @since 2018/3/12
 */
public class ValidException extends RuntimeException {
    public ValidException(String exceptionInfo) {
        super(exceptionInfo);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
