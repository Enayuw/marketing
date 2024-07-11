package com.br.marketing.check.job;

import com.br.marketing.check.beanhadler.DataCleanFactory;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.MarketingCleanDataTaskExample;
import com.br.marketing.entity.MarketingDataFileConfig;
import com.br.marketing.entity.MarketingDataFileConfigExample;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.MarketingDataFileConfigMapper;
import com.br.marketing.service.IDataCleaningGeneralService;
import com.br.marketing.service.IFileToMarketingRuleService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 通用的数据自动清洗job
 */
@Slf4j
@Component
public class DataCleaningAutoJob extends AbstractSimpleElasticJob {

    @Resource
    MarketingCleanDataTaskMapper marketingCleanDataTaskMapper;
    @Resource
    MarketingDataFileConfigMapper marketingDataFileConfigMapper;
    @Resource
    IDataCleaningGeneralService iDataCleaningGeneralService;

    @Autowired
    RedisChgService redisChgService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobContext) {
        // 查询待执行的配置表的需要自动执行的配置
        // 判断当前时间是否>=清洗开始时间 且小于清洗结束时间
        // 以配置id 为key 作为索引，进行抢锁
        // 抢到锁以后更新当前配置表为锁定状态
        // 释放锁


        while (true) {
//            redisChgService.lock("lock_key_clean_data:99999", "lock_key:99999");
            MarketingCleanDataTaskExample example = new MarketingCleanDataTaskExample();
            example.createCriteria()
                    .andCreateTimeLessThanOrEqualTo(new Date())
                    .andCleanStatusEqualTo(0)
                    .andAutoCleanWayTypeEqualTo(1)
                    .andIsDelEqualTo(1);
            example.setOrderByClause("create_time asc limit 1");
            List<MarketingCleanDataTask> marketingCleanDataTasks = marketingCleanDataTaskMapper.selectByExample(example);
            if (marketingCleanDataTasks.size() == 0) {
                break;
            }
            MarketingCleanDataTask marketingCleanDataTask = marketingCleanDataTasks.get(0);
            // 任务设置为清洗中
            marketingCleanDataTask.setCleanStatus(1);
            marketingCleanDataTaskMapper.updateByPrimaryKeySelective(marketingCleanDataTask);
//            redisChgService.unlock("lock_key_clean_data:99999", "lock_key:99999");

            try {
                // 执行清洗逻辑
                iDataCleaningGeneralService.autoCleanDataByTask(marketingCleanDataTask);
                // 更新任务为清洗完成
//                marketingCleanDataTask.setCleanStatus(2);
            } catch (Exception e) {
                // 更新任务为清洗完成
                marketingCleanDataTask.setCleanStatus(3);
            }
//            marketingCleanDataTaskMapper.updateByPrimaryKeySelective(marketingCleanDataTask);
        }



    }

}
