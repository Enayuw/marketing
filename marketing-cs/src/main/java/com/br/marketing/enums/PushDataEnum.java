package com.br.marketing.enums;

/**
 * @ClassName PushDataEnum
 * @Description 模拟数据入库成功，但返回异常入Pulsar的场景
 * @Author kongbx
 * @Date 2025/11/10 14:22
 */
public enum PushDataEnum {
    MARKETING_UPLOAD_BASE("marketing_upload_base","标准上传数据"),
    MARKETING_TRANSFER_BASE("marketing_transfer_base","标准转化数据");

    PushDataEnum(String value, String desc) {
        this.value = value;
        this.desc=desc;
    }

    private String value;

    private String desc;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
