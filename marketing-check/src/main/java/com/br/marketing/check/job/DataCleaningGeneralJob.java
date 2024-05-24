package com.br.marketing.check.job;

import com.br.marketing.service.IDataCleaningGeneralService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

import javax.annotation.Resource;

/**
 * 通用的数据清洗job
 * 目标：替代人工数据清洗，替代小程序功能，页面化操作
 * 功能：将现有在ftp目录中的上传文件通过清洗规则处理后，能够通过调用api接口最终实现数据入库（本期未实现：直接写入对应数据库表中）
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-24
 */
public class DataCleaningGeneralJob extends AbstractSimpleElasticJob {

    @Resource
    IDataCleaningGeneralService dataCleaningGeneralService;
    @Override
    public void process(JobExecutionMultipleShardingContext jobContext) {
        String jobParameter = jobContext.getJobParameter();

//        if(dataCleaningGeneralService.isAction()){
            dataCleaningGeneralService.action();
//        }

    }
}
