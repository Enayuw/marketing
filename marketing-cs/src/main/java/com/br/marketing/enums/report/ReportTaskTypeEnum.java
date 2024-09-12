package com.br.marketing.enums.report;

/**
 * 报表任务类型枚举
 *
 * @author zhen.li1
 * @dateTime 2024-09-02 14:45
 */
public enum ReportTaskTypeEnum {


    SCORE_MODEL_TYPE(1, "跑分模型分布"),
    XIECHENG_MONTH_TRANSFER_TYPE(2, "携程月转化报表"),
    XIECHENG_DAY_TRANSFER_TYPE(3, "携程日转化报表"),
    XIECHENG_WEEKLY_TRANSFER_TYPE(4, "携程7天滚动转化报表"),
    XIECHENG_COLLIDING_DAY_TYPE(5, "携程单日撞库结果分布"),
    XIECHENG_DATAUSE_TYPE(6, "携程数据使用率");


    ReportTaskTypeEnum(Integer value, String desc) {
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
