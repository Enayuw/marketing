package com.br.marketing.enums;

/**
 * 滴滴准入枚举
 */
public enum DiDiAllowMarketingEnum {

    YES(1),NO(2),NOKNOW(3);

    DiDiAllowMarketingEnum(Integer value){
        this.value = value;
    }

    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
