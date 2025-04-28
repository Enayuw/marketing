package com.br.marketing.enums;

/**
 *
 */
public enum TcTransferRecordStatusEnum {

    ACCESS_IN(0,"接入中"),
    ACCESS_SUCCESS(1,"接入成功"),
    ACCESS_FAIL(2,"接入失败"),
    CLEAN_IN(3,"清洗中"),
    CLEAN_COMPLETED(4,"清洗完成");
    TcTransferRecordStatusEnum(Integer value, String desc){
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
