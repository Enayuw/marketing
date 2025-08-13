package com.br.marketing.enums;

/**
 * sync_data_deal_status枚举
 */
public enum TcCpaSyncDealStatusEnum {

    DEAL_NO(0,"未完成"),
    DEAL_MIDDLE(1,"中间态"),
    DEAL_SUCCESS(2,"完成"),
    DEAL_FAIL(3,"失败"),
    NO_FILE(4,"文件不存在");
    TcCpaSyncDealStatusEnum(Integer value, String desc){
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
