package com.br.marketing.enums;

public enum CustomerQueueEnum {
    ORG_SYNC(1,"原始上传数据"),
    ORG_TRANSFER(2,"原始转化数据");

    CustomerQueueEnum(Integer value, String desc) {
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
