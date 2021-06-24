package com.br.marketing.common.commondto;


public enum ResultCode {

    SUCCESS(1),
    FAIL(0),
    INTERNAL_SERVER_ERROR(500);

    private Integer value;

    public Integer getValue() {
        return value;
    }

    ResultCode(Integer value) {
        this.value = value;
    }
}
