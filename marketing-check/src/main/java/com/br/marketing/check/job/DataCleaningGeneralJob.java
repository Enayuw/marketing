package com.br.marketing.check.job;

import com.br.marketing.check.beanhadler.DataCleanFactory;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.MarketingDataFileConfig;
import com.br.marketing.mapper.MarketingDataFileConfigMapper;
import com.br.marketing.service.IDataCleaningGeneralService;
import com.br.marketing.service.IFileToMarketingRuleService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 通用的数据清洗job
 * 目标：替代人工数据清洗，替代小程序功能，页面化操作
 * 功能：将现有在ftp目录中的上传文件通过清洗规则处理后，能够通过调用api接口最终实现数据入库（本期未实现：直接写入对应数据库表中）
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-24
 */
@Slf4j
@Component
public class DataCleaningGeneralJob extends AbstractSimpleElasticJob {

    @Resource
    IDataCleaningGeneralService dataCleaningGeneralService;
    @Resource
    MarketingDataFileConfigMapper marketingDataFileConfigMapper;
    @Resource
    DataCleanFactory dataCleanFactory;

    @Override
    public void process(JobExecutionMultipleShardingContext jobContext) {
        String jobParameter = jobContext.getJobParameter();
        log.warn("DataCleaningGeneralJob--开始执行-{}",jobParameter);
        MarketingCleanDataTask task = dataCleaningGeneralService.getAction();
        if(null != task){
            MarketingDataFileConfig config = marketingDataFileConfigMapper.selectByPrimaryKey(task.getConfigId());
            IFileToMarketingRuleService fileToMarketingRuleService = dataCleanFactory.getFileToMarketingRuleService(config);
            dataCleaningGeneralService.action(task,fileToMarketingRuleService,config);
        }else{
            log.warn("未找到满足条件的数据清洗任务");
        }
        log.warn("DataCleaningGeneralJob--执行结束-{}",jobParameter);
    }

}
