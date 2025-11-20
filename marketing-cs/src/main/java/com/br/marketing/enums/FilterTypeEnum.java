package com.br.marketing.enums;

public enum FilterTypeEnum {

    GENERAL_POLICY(0, "通用推决策"),
    XIECHENG_POLICY(1, "携程撞库结果推决策"),
    MERGE_POLICY(2, "合并数据推决策"),
    HALO_CALLBACK(3, "哈啰回调"),
    UPLOAD_RE_POLICY(4, "上传数据重推决策");

    FilterTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
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
