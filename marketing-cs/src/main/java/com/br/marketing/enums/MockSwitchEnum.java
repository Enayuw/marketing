package com.br.marketing.enums;

/**
 * @ClassName MockSwitchEnum
 * @Description 规则中心推决策挡板
 * @Author kongbx
 * @Date 2025/1/12 16:43
 */
public enum MockSwitchEnum {

    GENERAL("general","通用跑分"),
    XIECHENG("xiecheng","携程撞库结果筛选推决策"),
    HALO("halo","哈啰硅基人回调"),
    ESRETRY("esRetry","es异常"),
    POLICYRETRY("policyRetry","推决策异常"),
    CALLBACKRETRY("callbackRetry","哈啰硅基人回调异常");

    MockSwitchEnum(String value, String desc) {
        this.value = value;
        this.desc=desc;
    }

    private String value;

    private String desc;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
