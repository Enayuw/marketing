package com.br.marketing.check.job.qifu.report.job;

import com.br.marketing.check.enums.EmailSubjectEnum;
import com.br.marketing.check.job.qifu.report.AbstractReportEmailJob;
import org.springframework.stereotype.Component;

/**
 * @ClassName QiFuCDZAnalysisReportEmailJob
 * @Author hang.zhou
 * @Date 2025/7/18
 */
@Component
public class QiFuCDZAnalysisReportEmailJob extends AbstractReportEmailJob {

    @Override
    protected String getReportType() {
        return EmailSubjectEnum.QIFU_ANALYSIS_REPORT_SUBJECT.getStrategyName();
    }
}
