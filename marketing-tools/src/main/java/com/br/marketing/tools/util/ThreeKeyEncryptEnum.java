package com.br.marketing.tools.util;

import java.util.Arrays;
import java.util.Optional;

/**
 * 3K加密类型枚举
 */
public enum ThreeKeyEncryptEnum {

    md5(1,"md5"),sha256(2,"sha");

    ThreeKeyEncryptEnum(Integer code, String value) {
        this.value = value;
        this.code = code;
    }

    private Integer code;

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static ThreeKeyEncryptEnum getThreeKeyEncryptEnum(Integer code){
        Optional<ThreeKeyEncryptEnum> first = Arrays.stream(ThreeKeyEncryptEnum.values()).filter(t -> t.getCode().equals(code)).findFirst();
        return first.isPresent()?first.get():null;
    }
}
