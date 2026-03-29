package com.br.marketing.check.job;

import com.br.marketing.check.service.FileNoHeaderToMarketingDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 文件无表头的转化数据清洗 Job：对无表头文件按列顺序解析并推送转化接口
 * 文件类型：原始数据文件(无表头)；配置表：b_marketing_data_file_config_no_header
 * 当文件列数与配置列数不一致时告警（逻辑在 FileNoHeaderToMarketingDataService）
 *
 * @author kongbx
 */
@Component
@Slf4j
public class FileNoHeaderToMarketingDataJob extends AbstractSimpleElasticJob {

    @Resource
    private FileNoHeaderToMarketingDataService fileNoHeaderToMarketingDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        fileNoHeaderToMarketingDataService.process(context.getJobParameter());
    }
}
