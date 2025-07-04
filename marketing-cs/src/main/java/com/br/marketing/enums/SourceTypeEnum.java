package com.br.marketing.enums;

import lombok.Getter;

/**
 * 数据源类型枚举
 */
@Getter
public enum SourceTypeEnum {
    
    UPLOAD("UPLOAD", "上传"),
    TRANSFORM("TRANSFORM", "转化"),
    CALL("CALL", "外呼"),
    SHORTLINK("SHORTLINK","短链"),
    CALLBACK("CALLBACK", "回调"),
    KNOWLEDGE("KNOWLEDGE", "知识库");

    /**
     * 数据源编码
     */
    private final String code;
    
    /**
     * 数据源名称
     */
    private final String name;

    SourceTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据编码获取枚举
     */
    public static SourceTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (SourceTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断编码是否有效
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }

    /**
     * 获取所有数据源编码
     */
    public static String[] getCodes() {
        SourceTypeEnum[] values = values();
        String[] codes = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            codes[i] = values[i].getCode();
        }
        return codes;
    }

    /**
     * 获取所有数据源名称
     */
    public static String[] getNames() {
        SourceTypeEnum[] values = values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].getName();
        }
        return names;
    }

    @Override
    public String toString() {
        return this.code;
    }
} 