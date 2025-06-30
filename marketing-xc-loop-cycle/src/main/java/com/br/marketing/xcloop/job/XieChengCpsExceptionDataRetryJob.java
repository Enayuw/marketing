package com.br.marketing.xcloop.job;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengServiceNew;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.entity.XieChengCollidingDataLogExample;
import com.br.marketing.entity.XieChengCpsCollidingDataLoopCycleExample;
import com.br.marketing.entity.XieChengCpsCollidingDataRobExample;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;
import com.br.marketing.mapper.XieChengCpsCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCpsCollidingDataRobMapper;
import com.br.marketing.service.Impl.VariableAllocationServiceImpl;
import com.br.marketing.service.Impl.xc.XieChengCpsExceptionDataRetryService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 携程CPS异常重试作业
 * @Author chenh
 * @Date 2025-06-26
 */
@Component
@Slf4j
public class XieChengCpsExceptionDataRetryJob extends AbstractSimpleElasticJob {
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    XieChengCpsExceptionDataRetryService service;
    @Resource
    XieChengServiceNew xieChengServiceNew;

    @Autowired
    RedisChgService redisChgService;
    @Resource
    XieChengCpsCollidingDataLoopCycleMapper loopCycleMapper;
    @Resource
    XieChengCpsCollidingDataRobMapper robMapper;
    @Resource
    XieChengCollidingDataLogMapper logMapper;
    @Resource
    private VariableAllocationServiceImpl variableAllocationService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        service.process();
        log.warn("携程CPS异常重试作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}