package com.br.marketing.enums;

/**
 * clean_status枚举
 */
public enum TcCpaCleanStatusEnum {

    CLEAN_VOID(0,"待清洗"),
    CLEANING(1,"清洗中"),
    CLEAN_SUCCESS(2,"清洗成功"),
    CLEAN_FAIL(3,"清洗失败"),
    CLEAN_RETRY(4,"清洗重试")
    ;

    TcCpaCleanStatusEnum(Integer value, String desc){
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
