package com.br.marketing.entity;

/**
 * @Author: Bairong
 * @Time: 2021/2/22 15:07
 * @Company：百融
 * @Description: 监控类型枚举类
 */
public enum MonitorTypeEnum {
    PPD(1, "ppd"),
    CHG(2, "chg"),
    CHG360(2, "360"),
    HNNX(3, "hnnx"),
    MARKETING(4, "marketing"),
    STATUS_1(1, "正常"),
    STATUS_2(2, "剔除"),
    FAIL_TYPE_1(1, "MD5"),
    FAIL_TYPE_2(2, "Sha256");
    private int typeCode;
    private String type;

    MonitorTypeEnum(int typeCode, String type) {
        this.typeCode = typeCode;
        this.type = type;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public String getType() {
        return type;
    }
}
