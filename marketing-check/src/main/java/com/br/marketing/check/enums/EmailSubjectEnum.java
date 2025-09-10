package com.br.marketing.check.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * EmailSubjectEnum:邮件主题枚举
 *
 * @author zhen.Li1
 * @date 2024/03/26
 */
@Getter
@AllArgsConstructor
public enum EmailSubjectEnum {

    QIFU_STRATEGYREPORT_SUNJECT(1,"qiFuCWJStrategyReportStrategy", "360日统计报表"),
    QIFU_ANALYSIS_REPORT_SUBJECT(2,"qiFuCDZAnalysisReportStrategy","360促动分析效果统计数据报表"),
    QIFU_ROBOT_RANKING_REPORT_SUBJECT(3,"qiFuAiRobotRankingReportStrategy","360AI语音机器人排名报表"),
    QIFU_EFFECT_REPORT_SUBJECT(4,"qiFuEffectReportStrategy","360促申效果统计数据报表")
    ;

    private Integer value;
    private String strategyName;
    private String desc;

    public static EmailSubjectEnum getEnum(Integer value) {
        for (EmailSubjectEnum e : EmailSubjectEnum.values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
