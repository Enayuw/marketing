package com.br.marketing.enums;

import lombok.Getter;

/**
 * 数据源类型枚举
 */
@Getter
public enum SourceTypeEnum {
    
    UPLOAD("UPLOAD", "上传"){
        @Override
        public String getCellField(){
            return "";
        }

        @Override
        public String getTimeField() {
            return "";
        }

        @Override
        public String getCustNumField() {
            return "";
        }
    },
    TRANSFORM("TRANSFORM", "转化"){
        @Override
        public String getCellField(){
            return "cell";
        }

        @Override
        public String getTimeField(){
            return "create_time";
        }

        @Override
        public String getCustNumField(){
            return "cust_num";
        }
    },
    CALL("CALL", "外呼"){
        @Override
        public String getCellField(){
            return "phone_num_encoded";
        }

        @Override
        public String getTimeField(){
            return "case_log_create_time";
        }

        @Override
        public String getCustNumField(){
            return "case_num";
        }
    },
    SHORTLINK("SHORTLINK","短链"){
        @Override
        public String getCellField(){
            return "target_key";
        }

        @Override
        public String getTimeField() {
            return "create_time";
        }

        @Override
        public String getCustNumField() {
            return "";
        }
    },
    CALLBACK("CALLBACK", "回调"){
        @Override
        public String getCellField(){
            return "";
        }

        @Override
        public String getTimeField() {
            return "";
        }

        @Override
        public String getCustNumField() {
            return "";
        }
    },
    KNOWLEDGE("KNOWLEDGE", "知识库"){
        @Override
        public String getCellField(){
            return "";
        }

        @Override
        public String getTimeField() {
            return "";
        }

        @Override
        public String getCustNumField() {
            return "";
        }
    };

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

    public abstract String getCellField();

    public abstract String getTimeField();

    public abstract String getCustNumField();

    public static SourceTypeEnum fromCode(String code) {
        for (SourceTypeEnum sourceCode : SourceTypeEnum.values()) {
            if (sourceCode.getCode().equals(code)) {
                return sourceCode;
            }
        }
        return null;
    }
} 