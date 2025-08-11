package com.br.marketing.enums;

/**
 * TcCpa文件类型
 */
public enum TcCpaRecordTypeEnum {

    SYNC_RECORD(1,"sync_reocrd"),
    FAIL_RECORD(2,"fail_record");
    TcCpaRecordTypeEnum(Integer value, String desc){
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
