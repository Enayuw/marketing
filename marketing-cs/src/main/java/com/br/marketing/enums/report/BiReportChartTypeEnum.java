package com.br.marketing.enums.report;

import lombok.Getter;

/**
 * BI报表数据图表类型
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Getter
public enum BiReportChartTypeEnum {
    /**
     * 表格
     */
    TABLE(0, "table", "表格"),
    /**
     * 折线图
     */
    LINE(1, "line", "折线图"),
    /**
     * 柱状图
     */
    BAR(2, "bar", "柱状图"),
    /**
     * 饼图
     */
    PIE(3, "pie", "饼图"),
    ;

    private final Integer code;

    private final String type;

    private final String desc;

    BiReportChartTypeEnum(Integer code, String type, String desc) {
        this.code = code;
        this.type = type;
        this.desc = desc;
    }
}
