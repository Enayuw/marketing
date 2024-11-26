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
    XIECHENG_DATAUSE_TYPE(6, "携程数据使用率"),
    MULTPOINT_TYPE(7, "多头分布"),
    TRANSFER_ANALYSIS_TYPE(8, "转化分析"),
    GROUP_SCORE_TYPE(9, "分组评分分布"),
    TRACE_ANALYSIS_TYPE(10, "回溯分析"),
    OUTBOUND_STAT_TYPE(11, "外呼统计"),
    BUSINESS_ANALYSIS_ONE_TYPE(12, "经营分析1场景"),
    BUSINESS_ANALYSIS_SEVEN_TYPE(13, "经营分析7场景"),
    BUSINESS_ANALYSIS_EIGHT_TYPE(14, "经营分析8场景"),
    XIECHENG_MONTH_TRANSFER_CALL_TYPE(15, "携程月接通转化报表"),
    XIECHENG_DAY_TRANSFER_CALL_TYPE(16, "携程月接通转化报表"),
    TRANSFER_CONNECT_TYPE(17, "转化接通转化"),
    ;


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
