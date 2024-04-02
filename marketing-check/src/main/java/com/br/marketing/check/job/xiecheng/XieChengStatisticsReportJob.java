package com.br.marketing.check.job.xiecheng;

import com.br.marketing.check.service.email.IMailService;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingEmailSendConfig;
import com.br.marketing.entity.MarketingEmailSendConfigExample;
import com.br.marketing.entity.XieChengStatisticsReport;
import com.br.marketing.mapper.MarketingEmailSendConfigMapper;
import com.br.marketing.service.Impl.xc.XieChengStatisticsReporService;
import com.br.marketing.service.SyncConfigService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

/**
 * D20240320携程百万量级转化统计报表-3710058（邮件附件发送Excel）
 * 需求：https://c.100credit.cn/pages/viewpage.action?pageId=151480516
 * 技术文档：https://c.100credit.cn/pages/viewpage.action?pageId=155681800
 * 每天执行一次，将前一天的结果存库后再写到Excel中，最后发送邮件
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-03-29
 */
@Component
@Slf4j
public class XieChengStatisticsReportJob extends AbstractSimpleElasticJob {
    final static DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Resource
    private IMailService iMailService;
    @Resource
    private MarketingEmailSendConfigMapper marketingEmailSendConfigMapper;

    @Resource
    private XieChengStatisticsReporService xieChengStatisticsReporService;

    @Resource
    SyncConfigService syncConfigService;

    /**
     * job参数：
     * 说明： apiCode,cid,resultData(T),skipSendEmail (参数整体一次传一组)
     * 样例1： 3710058,643,2024-04-02,true (单独生成某个apiCode和对应cid的 T-1 数据,不发送邮件)
     * 样例2： 3710058,643,2024-04-01,false (每月月末触发job，生成每月的报表并发邮件)
     * @Author yu.xia@brgroup.com
     * @Date 2024/4/1 20:42
     * @param context
     */
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String uuid = UUID.randomUUID().toString();
        String jobParameter = context.getJobParameter();
        log.warn("XieChengStatisticsReportJob-start-{}-jobParam:[{}]",uuid,jobParameter);
        String apiCode = "3710058";
        Long cid = 643L;
        String resultData="";
        // 是否跳过生成Excel和发送邮件 判断标识
        String skipSendEmail = "false";
        if(StringUtils.isNotBlank(jobParameter)){
            String[] split = jobParameter.split(",");
            apiCode = split[0];
            cid = Long.valueOf(split[1]);
            resultData = split[2];
            skipSendEmail = split[3];
        }
//        String emailRecipient = "yu.xia@brgroup.com";
        String emailRecipient = "yu.xia@brgroup.com,bin.huang@brgroup.com";
//        String emailRecipient = "mmg@brgroup.com,yu.xia@brgroup.com,bin.huang@brgroup.com";
        // 路径
//        String excelFilePath = "D:\\test\\";
        String excelFilePath = syncConfigService.getPath().concat("excel/").concat("xc/").concat(apiCode).concat("/");
        // excel文件名
        String fileName;
        // 邮件主题
        String subject = "携程百万量级转化_";
        LocalDate today = LocalDate.now();
        if(StringUtils.isNotBlank(resultData)){
            try {
                // 使用parse方法将字符串转换为LocalDate
                today = LocalDate.parse(resultData, formatter);
            } catch (Exception e) {
                log.error("携程百万量级job参数处理出错-jobParameter[{}]--",jobParameter,e);
            }
        }
        String requestData = today.minusDays(1L).format(YMD);
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        String firstDayString;
        if (today.equals(firstDayOfMonth)) {
            // 今天是本月的第一天
            firstDayString = firstDayOfMonth.minusMonths(1L).format(YMD);
        } else {
            // 不是本月的第一天
            firstDayString = firstDayOfMonth.format(YMD);
        }

        MarketingEmailSendConfigExample marketingEmailSendConfigExample = new MarketingEmailSendConfigExample();
        marketingEmailSendConfigExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andIsDelEqualTo(1);
        marketingEmailSendConfigExample.setOrderByClause(" create_time desc ");
        List<MarketingEmailSendConfig> sendConfigList = marketingEmailSendConfigMapper.selectByExample(marketingEmailSendConfigExample);
        if(sendConfigList.size()>0){
            MarketingEmailSendConfig marketingEmailSendConfig = sendConfigList.get(0);
            emailRecipient = marketingEmailSendConfig.getReceiverUser();
            fileName = requestData+marketingEmailSendConfig.getAttachmentFileName();
        }else{
            fileName = requestData+subject+".xlsx";
        }
        String excelPath = excelFilePath + fileName;
        try{
            //1.执行sql查询前一天的数据，并写入数据库表 b_xiecheng_statistics_report 中
            xieChengStatisticsReporService.getUploadCountAndInsert(apiCode, cid, requestData);
            if("false".equalsIgnoreCase(skipSendEmail)){
                //2.读取数据库中的数据
                List<XieChengStatisticsReport> xieChengStatisticsReports =
                        xieChengStatisticsReporService.queryStatisticsReporDate(apiCode, firstDayString, requestData);
                //3.数据写入Excel
                xieChengStatisticsReporService.createExcel(xieChengStatisticsReports, excelFilePath, fileName);
                //4.邮件发送
                iMailService.sendAttachmentsMail(emailRecipient,subject+requestData,
                        "dear all: <br/>        "+apiCode+"-携程百万量级转化统计报表，详见附件", excelPath);
            }
        }catch (Exception e){
            log.error("携程百万量级转化统计报表处理异常:requestData[{}]firstDayString[{}]--", requestData, firstDayString, e);
        }finally {
            //5.删除Excel
            if("false".equalsIgnoreCase(skipSendEmail)){
                try {
                    File file = new File(excelPath);
                    Path path = file.toPath();
                    Files.deleteIfExists(path);
                } catch (Exception e) {
                    log.error("携程百万量级转化统计[{}]Files.delete error--", excelPath, e);
                }
            }
        }
        log.warn("TableBackupJob-end-{}apiCode[{}]cid[{}]requestData[{}]skipSendEmail[{}]",
                uuid,apiCode,cid,requestData,skipSendEmail);
    }
}
