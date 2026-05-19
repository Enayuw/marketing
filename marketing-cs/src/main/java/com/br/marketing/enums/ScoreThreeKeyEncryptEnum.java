package com.br.marketing.enums;

public enum ScoreThreeKeyEncryptEnum {

    init(0),md5(1),sha256(2),general(3);

    ScoreThreeKeyEncryptEnum(Integer value) {
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
