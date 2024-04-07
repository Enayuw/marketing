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

    QIFU_STRATEGYREPORT_SUNJECT(1, "360日统计报表"),
    ;

    private Integer value;
    private String desc;
}
