package com.br.marketing.enums.report;

import lombok.Getter;

/**
 * 营销报表类型枚举
 * <p>
 * 枚举名称规范: ${客户简称全拼}_${数据类型}_${报表维度}_REPORT
 * 数据类型: 上传-UPLOAD;转化-TRANSFER;撞库-COLLIDING;
 * 报表维度： 日（按日）-DAILY;周(按周)-WEEKLY;月（按月）-MONTHLY
 * <p>
 * 枚举Code规范：携程号段：100-199;新增客户依次新增（示例：200-299）
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Getter
public enum BiReportTypeEnum {

    /**
     * 携程月转化报表
     */
    XIECHENG_TRANSFER_MONTHLY_REPORT(100, 2, "xiechengTransferMonthlyReport"),
    /**
     * 携程日转化报表
     */
    XIECHENG_TRANSFER_DAILY_REPORT(101, 3, "xiechengTransferDailyReport"),
    /**
     * 携程7日滚动转化报表
     */
    XIECHENG_TRANSFER_WEEKLY_REPORT(102, 4, "xiechengTransferWeeklyReport"),
    /**
     * 携程单日撞库结果分布报表
     */
    XIECHENG_COLLIDING_DAILY_REPORT(103, 5, "xiechengCollidingDailyReport"),
    /**
     * 携程7日撞库结果分布报表
     */
    XIECHENG_COLLIDING_WEEKLY_REPORT(104, null, "xiechengCollidingWeeklyReport"),
    /**
     * 携程数据使用率报表
     */
    XIECHENG_DATARATIO_DAILY_REPORT(105, 6, "xiechengDataRatioDailyReport"),
    /**
     * 多头分布报表
     */
    MULTPOINT_REPORT(106, 7, "multPointReport"),
    /**
     * 转化分析报表
     */
    TRANSFER_ANALYSIS_REPORT(107, 8, "transferAnalysisReport"),
    /**
     * 分组评分分布报表
     */
    GROUP_SCORE_REPORT(108, 9, "groupScoreReport"),
    /**
     * 回溯分析报表
     */
    TRACE_ANALYSIS_REPORT(109, 10, "traceAnalysisReport"),
    /**
     * 外呼统计报表
     */
    OUTBOUND_STAT_REPORT(110, 11, "outboundStatReport"),
    /**
     * 经营分析1场景报表
     */
    BUSINESS_ANALYSIS_ONE_REPORT(111, 12, "businessAnalysisOneReport"),
    /**
     * 经营分析7场景报表
     */
    BUSINESS_ANALYSIS_SEVEN_REPORT(112, 12, "businessAnalysisSevenReport"),
    /**
     * 经营分析8场景报表
     */
    BUSINESS_ANALYSIS_EIGHT_REPORT(113, 12, "businessAnalysisEightReport"),
    ;

    /** code */
    private final Integer code;

    private final Integer type;

    /** 名称 */
    private final String typeName;

    BiReportTypeEnum(Integer code, Integer type, String typeName) {
        this.code = code;
        this.type = type;
        this.typeName = typeName;
    }

    /**
     * 按名称获取枚举
     *
     * @param typeName 报表类型名称
     * @return {@link BiReportTypeEnum }
     * @author senyang.zheng
     * @date 2024/08/28
     */
    public static BiReportTypeEnum getEnumByTypeName(String typeName) {
        for (BiReportTypeEnum enumValue : BiReportTypeEnum.values()) {
            if (enumValue.getTypeName().equals(typeName)) {
                return enumValue;
            }
        }
        return null;
    }
}
