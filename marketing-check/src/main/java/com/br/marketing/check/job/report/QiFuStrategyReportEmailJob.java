package com.br.marketing.check.job.report;

import com.alibaba.excel.EasyExcel;
import com.br.marketing.check.service.email.IMailService;
import com.br.marketing.entity.QifuStrategyReportData;
import com.br.marketing.entity.QifuStrategyReportDataExample;
import com.br.marketing.entity.excel.QiFuStrategyReportExcelModel;
import com.br.marketing.mapper.QifuStrategyReportDataMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
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
    private IMailService mailService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        //生成excel
        try {
            String subject = "360日统计报表";
            String fileName = "360日统计报表.xlsx";
            // 生成文件
            String excelFilePath = "/opt/data/360/" + fileName;
            QifuStrategyReportDataExample qifuStrategyReportDataExample = new QifuStrategyReportDataExample();
            qifuStrategyReportDataExample.createCriteria()
                    .andUpdateDateEqualTo(LocalDate.now().toString());
            List<QifuStrategyReportData> reportDataList = qifuStrategyReportDataMapper.selectByExample(qifuStrategyReportDataExample);
            List<QiFuStrategyReportExcelModel> reportExcelModelList = reportDataList.stream()
                    .map(reportData -> {
                        QiFuStrategyReportExcelModel reportExcelModel = new QiFuStrategyReportExcelModel();
                        BeanUtils.copyProperties(reportData, reportExcelModel);
                        return reportExcelModel;
                    }).collect(Collectors.toList());

            EasyExcel.write(excelFilePath, QiFuStrategyReportExcelModel.class).sheet("360日报表").doWrite(reportExcelModelList);
            //发送Email
            mailService.sendAttachmentsMail("zhen.li1@brgroup.com", subject, subject + ": 策略效果数据报表"
                    , excelFilePath, "360日统计报表");

        } catch (Exception e) {
            log.error("360 策略效果数据发送Email失败{}", e);
        }


    }
}
