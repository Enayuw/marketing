package com.br.marketing.common.commondto;


public enum ResultCode {

    SUCCESS(1),FAIL(500);

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    private Integer value;

    ResultCode(Integer value){
        this.value = value;
    }


}
