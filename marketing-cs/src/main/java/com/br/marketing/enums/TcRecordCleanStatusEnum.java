package com.br.marketing.enums;

/**
 *
 */
public enum TcRecordCleanStatusEnum {

    CLEAN_WAITED(0,"待清洗"),
    CLEAN_COMPLETED(1,"清洗完成"),
    CLEAN_CLEAN_FAIL(2,"清洗失败"),
    CLEAN_PUSH(3,"清洗推送失败"),
    CLEAN_EXCEPTION(4,"清洗异常");
    TcRecordCleanStatusEnum(Integer value, String desc){
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
