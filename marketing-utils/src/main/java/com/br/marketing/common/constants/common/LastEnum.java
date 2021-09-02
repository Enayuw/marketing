package com.br.marketing.common.constants.common;

import java.util.HashMap;
import java.util.Map;

public enum LastEnum {
    IS_LAST("1"), NOT_LAST("0");

    private String code;

    LastEnum(String code) {
        this.code = code;
    }

    private static Map<String, LastEnum> map = new HashMap<>(2, 1);

    static {
        for (LastEnum lastEnum : LastEnum.values()) {
            map.put(lastEnum.getCode(), lastEnum);
        }
    }

    public static LastEnum getByCode(String code) {
        return map.get(code);
    }

    public static boolean isLegal(String code) {
        return null != map.get(code);
    }

    public String getCode() {
        return code;
    }
}
