package com.br.marketing.enums;

/**
 * sync_data_collect_status枚举
 */
public enum TcCpaCollectStatusEnum {

    DEAL_NO(0,"待统计"),
    DEAL_MIDDLE(1,"统计中"),
    DEAL_SUCCESS(2,"统计完成"),
    DEAL_FAIL(3,"统计失败"),
    ;
    TcCpaCollectStatusEnum(Integer value, String desc){
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
