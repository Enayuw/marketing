package com.br.marketing.enums;

/**
 *
 */
public enum TcCpaMatchStatusEnum {

    MATCH_NO(0,"未匹配"),
    MATCH_SUCCESS(1,"匹配"),
    MATCH_ING(2,"匹配中");
    TcCpaMatchStatusEnum(Integer value, String desc){
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
