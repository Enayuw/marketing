package com.br.marketing.check.job.qifu.report.job;

import com.br.marketing.check.enums.EmailSubjectEnum;
import com.br.marketing.check.job.qifu.report.AbstractReportEmailJob;
import org.springframework.stereotype.Component;

/**
 * @ClassName QiFuAiRobotRankingReportEmailJob
 * @Author hang.zhou
 * @Date 2025/7/29
 */
@Component
public class QiFuAiRobotRankingReportEmailJob extends AbstractReportEmailJob {
    @Override
    protected String getReportType() {
        return EmailSubjectEnum.QIFU_ROBOT_RANKING_REPORT_SUBJECT.getStrategyName();
    }
}
