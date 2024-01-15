package com.br.marketing.check.job;

import com.br.marketing.check.service.PushCustomerService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.Customer;
import com.br.marketing.entity.ScorePushCustomerConfig;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.mapper.CustomerMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


/**
 * 跑批分数回调
 */
@Component
@Slf4j
public class PushCustomerJob extends AbstractSimpleElasticJob {
    @Resource
    PushCustomerService pushCustomerService;
    @Resource
    CustomerMapper customerMapper;

    /**
     * 1、获取跑批配置
     * 2、判断当前回调配置是否需要执行
     * 3、执行回调任务
     *      pushStatus 1-回调成功；2-回调有失败记录；3-更新排序成功；4-更新排序失败；5-任务执行中
     * @param jobExecutionMultipleShardingContext
     */
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Long start = System.currentTimeMillis();
        log.warn("【api推送客户数据】调度开始");
        try {
            List<ScorePushCustomerConfig> scorePushConfigs = pushCustomerService.getScorePushConfigs();
            for (ScorePushCustomerConfig scorePushConfig : scorePushConfigs) {
                Result<StraHisFile> configRes = pushCustomerService.isPush(scorePushConfig);
                if(ResultCode.SUCCESS.getValue().equals(configRes.getCode())){
                    pushCustomerService.push(scorePushConfig,configRes.getData());
                }
            }
        } catch (Exception ex) {
            log.error("跑分推送客户报错" + ex.getMessage(), ex);
        }
        Long end = System.currentTimeMillis();
        log.warn("【结果文件校验api推送客户数据】调度结束，耗时：{}", end - start);

    }
}
