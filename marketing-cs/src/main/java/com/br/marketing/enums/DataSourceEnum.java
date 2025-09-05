package com.br.marketing.enums;

/**
 * 数据源枚举
 */
public enum DataSourceEnum {

    MARKETING_TIKV(1, "marketing", "marketing数据源"),
    MARKETING_DORIS(2, "marketingDoris", "Doris数据源"),
    MARKETING_BI(3, "marketingBI", "BI数据源"),
    MARKETING_LOG(4, "marketingLog", "日志数据源"),
    MARKETING_LINK(5, "marketingLink", "短链数据源");

    DataSourceEnum(Integer code, String sourceCode, String desc) {
        this.code = code;
        this.sourceCode = sourceCode;
        this.desc = desc;
    }

    private Integer code;

    private String sourceCode;

    private String desc;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举
     */
    public static DataSourceEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DataSourceEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据源码获取枚举
     */
    public static DataSourceEnum getBySourceCode(String sourceCode) {
        if (sourceCode == null) {
            return null;
        }
        for (DataSourceEnum type : values()) {
            if (type.getSourceCode().equals(sourceCode)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断编码是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 判断源码是否有效
     */
    public static boolean isValidSourceCode(String sourceCode) {
        return getBySourceCode(sourceCode) != null;
    }

    /**
     * 获取描述
     */
    public static String getDescByCode(Integer code) {
        DataSourceEnum type = getByCode(code);
        return type != null ? type.getDesc() : null;
    }

    /**
     * 获取描述
     */
    public static String getDescBySourceCode(String sourceCode) {
        DataSourceEnum type = getBySourceCode(sourceCode);
        return type != null ? type.getDesc() : null;
    }
}