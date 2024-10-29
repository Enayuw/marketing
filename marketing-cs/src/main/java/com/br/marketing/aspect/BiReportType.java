package com.br.marketing.aspect;

import com.br.marketing.enums.report.BiReportTypeEnum;

import java.lang.annotation.*;

/**
 * 双向报告类型
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface BiReportType {
    BiReportTypeEnum reportType();
}
