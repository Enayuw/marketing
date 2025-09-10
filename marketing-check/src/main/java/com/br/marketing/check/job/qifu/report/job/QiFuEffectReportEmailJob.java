package com.br.marketing.check.job.qifu.report.job;

import com.br.marketing.check.enums.EmailSubjectEnum;
import com.br.marketing.check.job.qifu.report.AbstractReportEmailJob;
import org.springframework.stereotype.Component;

/**
 * @ClassName QiFuEffectReportEmailJob
 * @Author hang.zhou
 * @Date 2025/8/13
 */
@Component
public class QiFuEffectReportEmailJob extends AbstractReportEmailJob {
    @Override
    protected String getReportType() {
         return EmailSubjectEnum.QIFU_EFFECT_REPORT_SUBJECT.getStrategyName();
    }
}
