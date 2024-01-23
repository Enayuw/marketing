package com.br.marketing.enums;

public enum CallBackPushStatusEnum {

    TOBEEXECUTED(0,"待执行")
    ,SUCCESS(1,"回调成功")
    ,CALLBACKFAIL(2,"回调客户接口失败")
    ,GETFAIL(3,"数据捞取错误")
    ,SORTFAIL(4,"更新排序失败")
    ,SORTOK(5,"更新排序成功")
    ,STARTING(6,"回调开始执行中");

    CallBackPushStatusEnum(Integer value, String desc) {
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
