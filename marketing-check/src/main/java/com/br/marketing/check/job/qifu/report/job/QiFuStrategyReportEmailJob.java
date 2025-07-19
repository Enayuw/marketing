package com.br.marketing.check.job.qifu.report.job;

import com.br.marketing.check.enums.EmailSubjectEnum;
import com.br.marketing.check.job.qifu.report.AbstractReportEmailJob;
import org.springframework.stereotype.Component;

/**
 * @ClassName QiFuStrategyReportEmailJob
 * @Author hang.zhou
 * @Date 2025/7/18
 */
@Component
public class QiFuStrategyReportEmailJob extends AbstractReportEmailJob {

    @Override
    protected String getReportType() {
        return EmailSubjectEnum.QIFU_STRATEGYREPORT_SUNJECT.getStrategyName();
    }
} 