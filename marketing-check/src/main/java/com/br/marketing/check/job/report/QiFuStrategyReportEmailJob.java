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
import com.br.marketing.service.SyncConfigService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
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
 * 20240723增加数据接收日期 https://c.100credit.cn/pages/viewpage.action?pageId=171450464
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

    @Autowired
    SyncConfigService syncConfigService;


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
            List<MarketingEmailSendConfig> sendConfigList = marketingEmailSendConfigMapper.selectByExample(marketingEmailSendConfigExample);
            if (CollectionUtils.isEmpty(sendConfigList)) {
                log.warn("360策略效果数据-邮件配置为空");
                return;
            }
            MarketingEmailSendConfig sendConfig = sendConfigList.get(0);
            String subject = EmailSubjectEnum.QIFU_STRATEGYREPORT_SUNJECT.getDesc() + "_" + LocalDate.now();
            // 生成文件
            String excelPath = syncConfigService.getPath().concat("excel/").concat("360/").concat(apiCode).concat("/");
            File excelDic = new File(excelPath);
            if (!excelDic.exists()) {
                excelDic.mkdirs();
            }
            String excelFilePath = excelPath + sendConfig.getAttachmentFileName();
            QifuStrategyReportDataExample qifuStrategyReportDataExample = new QifuStrategyReportDataExample();
            qifuStrategyReportDataExample.createCriteria().andApiCodeEqualTo(apiCode)
                    .andCreateTimeGreaterThan(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
            List<QifuStrategyReportData> reportDataList = qifuStrategyReportDataMapper.selectByExample(qifuStrategyReportDataExample);
            List<QiFuStrategyReportExcelModel> reportExcelModelList = reportDataList.stream()
                    .map((QifuStrategyReportData reportData) -> {
                        QiFuStrategyReportExcelModel reportExcelModel = new QiFuStrategyReportExcelModel();
                        BeanUtils.copyProperties(reportData, reportExcelModel);
                        reportExcelModel.setStrategyDate(LocalDate.now().toString());
                        return reportExcelModel;
                    }).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(reportExcelModelList)) {
                log.error("360策略效果数据未传输，请检查");
            }
            EasyExcel.write(excelFilePath, QiFuStrategyReportExcelModel.class).sheet(EmailSubjectEnum.QIFU_STRATEGYREPORT_SUNJECT.getDesc()).
                    doWrite(reportExcelModelList);
            //发送Email
            mailService.sendAttachmentsMail(sendConfig.getReceiverUser(), subject, subject + ": 策略效果数据报表"
                    , excelFilePath, sendConfig.getAttachmentFileName());
        } catch (Exception e) {
            log.error("360 策略效果数据发送Email失败{}", e);
        }
    }

}
