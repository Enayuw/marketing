package com.br.marketing.check.job;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.MarketingCleanDataTaskExample;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.service.DataCleaningAutoService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.br.marketing.common.constants.rediskey.RedisKeyConstant.LOCK_KEY_CLEAN_DATA;

/**
 * 通用的数据自动清洗job
 */
@Slf4j
@Component
public class DataCleaningAutoJob extends AbstractSimpleElasticJob {

    @Resource
    MarketingCleanDataTaskMapper marketingCleanDataTaskMapper;

    @Resource
    DataCleaningAutoService dataCleaningAutoService;

    @Resource
    RedisChgService redisChgService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobContext) {
        while (true) {
            MarketingCleanDataTask marketingCleanDataTask = getMarketingCleanDataTask();
            if (marketingCleanDataTask == null) {
                break;
            }
            dataCleaningAutoService.autoCleanDataByTask(marketingCleanDataTask);
        }
    }

    private MarketingCleanDataTask getMarketingCleanDataTask() {
        redisChgService.lock(LOCK_KEY_CLEAN_DATA, LOCK_KEY_CLEAN_DATA+"9999");
        try {
            MarketingCleanDataTaskExample example = new MarketingCleanDataTaskExample();
            exampleCreateCriteria(example);
            List<MarketingCleanDataTask> marketingCleanDataTasks = marketingCleanDataTaskMapper.selectByExample(example);
            if (marketingCleanDataTasks.isEmpty()) {
                return null;
            }
            MarketingCleanDataTask marketingCleanDataTask = marketingCleanDataTasks.get(0);
            // 任务设置为清洗中
            marketingCleanDataTask.setCleanStatus(1);
            marketingCleanDataTaskMapper.updateByPrimaryKeySelective(marketingCleanDataTask);
            redisChgService.unlock(LOCK_KEY_CLEAN_DATA, LOCK_KEY_CLEAN_DATA+"9999");
            return marketingCleanDataTask;
        }catch (Exception e){
            redisChgService.unlock(LOCK_KEY_CLEAN_DATA, LOCK_KEY_CLEAN_DATA+"9999");
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), "清洗任务异常，Redis 锁已经释放！"));
            return null;
        }
    }

    private static void exampleCreateCriteria(MarketingCleanDataTaskExample example) {
        example.createCriteria()
                .andCreateTimeLessThanOrEqualTo(new Date())
                .andCleanStatusIn(Lists.newArrayList(0, 3))
                .andAutoCleanWayTypeEqualTo(1)
                .andIsDelEqualTo(1);
        example.setOrderByClause("create_time asc limit 1");
    }

}
