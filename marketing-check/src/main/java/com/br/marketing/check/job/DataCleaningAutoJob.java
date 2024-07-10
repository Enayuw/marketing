package com.br.marketing.check.job;

import com.br.marketing.check.beanhadler.DataCleanFactory;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.MarketingDataFileConfig;
import com.br.marketing.mapper.MarketingDataFileConfigMapper;
import com.br.marketing.service.IDataCleaningGeneralService;
import com.br.marketing.service.IFileToMarketingRuleService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 通用的数据自动清洗job
 */
@Slf4j
@Component
public class DataCleaningAutoJob extends AbstractSimpleElasticJob {

    @Resource
    IDataCleaningGeneralService dataCleaningGeneralService;
    @Resource
    MarketingDataFileConfigMapper marketingDataFileConfigMapper;
    @Resource
    DataCleanFactory dataCleanFactory;

    @Autowired
    RedisChgService redisChgService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobContext) {
        // 查询待执行的配置表的需要自动执行的配置
        // 判断当前时间是否>=清洗开始时间 且小于清洗结束时间
        // 以配置id 为key 作为索引，进行抢锁
        // 抢到锁以后更新当前配置表为锁定状态
        // 释放锁
        // 执行当前配置锁对应的清洗需求
        // while 循环执行清洗查询的sql
        // 获取到数据后，根据配置的的headerFiled 字段，获取数据
        // 根据配置的映射组装数据
        // while 循环结束后更新锁状态为释放
        List<Integer> shardingItems = jobContext.getShardingItems();
        redisChgService.lock("3333", "4444");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        redisChgService.unlock("3333","4444");
        log.warn("测试释放锁。。。。");

    }

}
