package com.br.marketing.enums;

/**
 * 推送文件状态枚举
 */
public enum TcCpaPushFileTaskStatusEnum {

    STATUS_GENINAG(1, "生成中"),
    STATUS_SUCCESS(2, "生成成功"),
    STATUS_FAIL(3, "生成失败"),
    STATUS_INNER_SFTP(4, "同步至内部sftp"),
    STATUS_OPE_SFTP(5, "同步至运营sftp");


    TcCpaPushFileTaskStatusEnum(Integer value, String desc){
        this.value = value;
        this.desc=desc;
    }

    private Integer value;

    private String desc;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
