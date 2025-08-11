package com.br.marketing.check.job.qifu.report;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.CollectionUtils;
import com.br.marketing.check.enums.EmailSubjectEnum;
import com.br.marketing.check.job.qifu.report.factory.ReportStrategyFactory;
import com.br.marketing.check.job.qifu.report.strategy.ReportStrategy;
import com.br.marketing.check.service.email.IMailService;
import com.br.marketing.entity.MarketingEmailSendConfig;
import com.br.marketing.entity.MarketingEmailSendConfigExample;
import com.br.marketing.mapper.MarketingEmailSendConfigMapper;
import com.br.marketing.service.SyncConfigService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName AbstractReportEmailJob
 * @Author hang.zhou
 * @Date 2025/7/18
 */
@Component
public abstract class AbstractReportEmailJob extends AbstractSimpleElasticJob {

    private static final Logger log = LoggerFactory.getLogger(AbstractReportEmailJob.class);

    @Autowired
    private ReportStrategyFactory reportStrategyFactory;

    @Autowired
    private IMailService mailService;

    @Autowired
    private MarketingEmailSendConfigMapper marketingEmailSendConfigMapper;

    @Autowired
    private SyncConfigService syncConfigService;

    /**
     * 指定报表类型
     */
    protected abstract String getReportType();

    @Override
    @SuppressWarnings("rawtypes")
    public void process(JobExecutionMultipleShardingContext context) {
        ReportStrategy strategy = reportStrategyFactory.getStrategy(getReportType());
        String apiCode = strategy.getApiCode(context);
        Integer subjectCode = strategy.getSubjectCode();

        // 获取邮件配置
        MarketingEmailSendConfigExample example = new MarketingEmailSendConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andSubjectEqualTo(subjectCode).andIsDelEqualTo(1);
        List<MarketingEmailSendConfig> sendConfigList = marketingEmailSendConfigMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(sendConfigList)) {
            log.warn("邮件配置为空");
            return;
        }
        MarketingEmailSendConfig sendConfig = sendConfigList.get(0);

        String currentDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
        List<?> dataList = strategy.queryData(apiCode, currentDate);
        List<?> excelList = strategy.convertToExcelModel(dataList);
        List<?> processedList = strategy.postProcess(excelList);
        if (CollectionUtils.isEmpty(processedList)) {
            log.error("报表数据未传输，请检查");
            return;
        }

        String subject = Objects.requireNonNull(EmailSubjectEnum.getEnum(sendConfig.getSubject())).getDesc();
        String excelPath = syncConfigService.getPath().concat("excel/").concat(apiCode).concat("/");

        File excelDic = new File(excelPath);
        if (!excelDic.exists()) {
            excelDic.mkdirs();
        }
        String fileName = strategy.getAttachmentFileName(subject).concat(".xlsx");
        String excelFilePath = excelPath + File.separator + fileName;

        // 生成Excel文件
        EasyExcel.write(excelFilePath, strategy.getExcelModelClass())
                .sheet(strategy.getSheetName())
                .doWrite(processedList);

        mailService.sendAttachmentsMail(sendConfig.getReceiverUser(), strategy.getAttachmentFileName(subject), strategy.getContent(subject), excelFilePath, fileName);
    }

}
