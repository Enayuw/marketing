package com.br.marketing.check.job.report;

import com.alibaba.excel.EasyExcel;
import com.br.marketing.check.enums.EmailSubjectEnum;
import com.br.marketing.check.service.email.IMailService;
import com.br.marketing.entity.MarketingEmailSendConfig;
import com.br.marketing.entity.MarketingEmailSendConfigExample;
import com.br.marketing.entity.QifuStrategyReportData;
import com.br.marketing.entity.QifuStrategyReportDataExample;
import com.br.marketing.entity.excel.QiFuStrategyReportExcelModel;
import com.br.marketing.mapper.MarketingEmailSendConfigMapper;
import com.br.marketing.mapper.QifuStrategyReportDataMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 360策略效果数据发送Email任务
 *
 * @author zhen.Li1
 * @dateTime 2024/03/23 14:07
 */
@Component
@Slf4j
public class QiFuStrategyReportEmailJob extends AbstractSimpleElasticJob {

    @Autowired
    private QifuStrategyReportDataMapper qifuStrategyReportDataMapper;
    @Autowired
    private MarketingEmailSendConfigMapper marketingEmailSendConfigMapper;

    @Autowired
    private IMailService mailService;

    @Value("${temp_file_path}")
    private String tempFilePath;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        //生成excel
        try {
            String jobParameter = context.getJobParameter();
            String apiCode;
            if (StringUtils.isNotEmpty(jobParameter)) {
                apiCode = jobParameter;
            } else {
                apiCode = "3710053";
            }
            MarketingEmailSendConfigExample marketingEmailSendConfigExample = new MarketingEmailSendConfigExample();
            marketingEmailSendConfigExample.createCriteria().andApiCodeEqualTo(apiCode)
                    .andSubjectEqualTo(EmailSubjectEnum.QIFU_STRATEGYREPORT_SUNJECT.getValue()).andIsDelEqualTo(1);
            MarketingEmailSendConfig sendConfig = marketingEmailSendConfigMapper.selectByExample(marketingEmailSendConfigExample).get(0);
            String subject = EmailSubjectEnum.QIFU_STRATEGYREPORT_SUNJECT.getDesc()+"_"+LocalDate.now().toString();
            // 生成文件
            String excelFilePath = tempFilePath + sendConfig.getAttachmentFileName();
            QifuStrategyReportDataExample qifuStrategyReportDataExample = new QifuStrategyReportDataExample();
            qifuStrategyReportDataExample.createCriteria().andApiCodeEqualTo(apiCode)
                    .andCreateTimeGreaterThan(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
            List<QifuStrategyReportData> reportDataList = qifuStrategyReportDataMapper.selectByExample(qifuStrategyReportDataExample);
            List<QiFuStrategyReportExcelModel> reportExcelModelList = reportDataList.stream()
                    .map((QifuStrategyReportData reportData) -> {
                        QiFuStrategyReportExcelModel reportExcelModel = new QiFuStrategyReportExcelModel();
                        BeanUtils.copyProperties(reportData, reportExcelModel);
                        return reportExcelModel;
                    }).collect(Collectors.toList());

            EasyExcel.write(excelFilePath, QiFuStrategyReportExcelModel.class).sheet(EmailSubjectEnum.QIFU_STRATEGYREPORT_SUNJECT.getDesc()).
                    doWrite(reportExcelModelList);
            //发送Email
            mailService.sendAttachmentsMail(sendConfig.getReceiverUser(), subject, subject + ": 策略效果数据报表"
                    , excelFilePath,sendConfig.getAttachmentFileName());

        } catch (Exception e) {
            log.error("360 策略效果数据发送Email失败{}", e);
        }

    }
}
