package com.br.marketing.enums.report;

/**
 * 报表任务状态枚举
 *
 * @author zhen.li1
 * @dateTime 2024-08-19 19:01
 */
public enum ReportTaskStatusEnum {

    READY(0, "待执行"),
    RUNNING(1, "统计中"),
    SUCCESS(2, "已完成"),
    FAIL(3, "统计失败");

    ReportTaskStatusEnum(Integer value, String desc) {
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
