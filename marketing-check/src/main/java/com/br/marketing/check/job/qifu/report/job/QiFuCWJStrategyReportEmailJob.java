package com.br.marketing.check.job.qifu.report.job;

import com.br.marketing.check.enums.EmailSubjectEnum;
import com.br.marketing.check.job.qifu.report.AbstractReportEmailJob;
import org.springframework.stereotype.Component;

/**
 * @ClassName QiFuCWJStrategyReportEmailJob
 * @Author hang.zhou
 * @Date 2025/7/18
 */
@Component
public class QiFuCWJStrategyReportEmailJob extends AbstractReportEmailJob {

    @Override
    protected String getReportType() {
        return EmailSubjectEnum.QIFU_STRATEGYREPORT_SUNJECT.getStrategyName();
    }
} 