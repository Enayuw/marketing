package com.br.marketing.xcloop.job;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengServiceNew;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengCollidingDataLogExample;
import com.br.marketing.entity.XieChengCollidingDataLoopCycleExample;
import com.br.marketing.entity.XieChengCollidingDataRobExample;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.service.Impl.VariableAllocationServiceImpl;
import com.br.marketing.service.Impl.xc.XcExceptionDataRetryService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @Description 携程异常重试作业
 * @Author hong.chen
 * @CreateTime 2024/03/20
 */
@Component
@Slf4j
public class XcExceptionDataRetryJob extends AbstractSimpleElasticJob {
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    XcExceptionDataRetryService service;
    @Resource
    XieChengServiceNew xieChengServiceNew;

    @Autowired
    RedisChgService redisChgService;
    @Resource
    XieChengCollidingDataLoopCycleMapper loopCycleMapper;

    @Resource
    XieChengCollidingDataRobMapper robMapper;

    @Resource
    XieChengCollidingDataLogMapper logMapper;
    @Resource
    private VariableAllocationServiceImpl variableAllocationService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        // 判断强制开启撞库开关
        if (marketingCommonConfig.getXieChengForceOpenSwitch() || canOpenConditionSwitch()) {
            // 执行重试撞库
            service.process();
        }
        log.warn("携程异常重试作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }

    /**
     * 校验是否需要开启条件开关
     * @return
     */
    private boolean canOpenConditionSwitch() {
        if (isShutDownConditionSwitch()) {
            xieChengServiceNew.shutDownConditionSwitch();
            return false;
        }

        // 获取条件开关，取不到报警
        String redisSwitch;
        try {
            redisSwitch = redisChgService.get(RedisKeyConstant.XIECHENG_CONDITIONSWITCH);
            log.warn("携程异常数据重试撞库，条件开关状态：{}", redisSwitch);
        } catch (Exception e) {
            log.error("携程异常数据重试撞库，获取redis条件开关失败:" + e.getMessage(), e);
            return false;
        }

        Boolean conditionSwitch = "false".equalsIgnoreCase(redisSwitch) || StringUtils.isEmpty(redisSwitch);
        if (conditionSwitch) {
            redisChgService.set(RedisKeyConstant.XIECHENG_CONDITIONSWITCH, "true");
        }
        return true;
    }


    /**
     * 判断是否需要打开条件开关
     * @return true:是，false：否
     */
    private boolean isShutDownConditionSwitch() {
        if (hasOverCountOfCode() || isOverCountOfTrue() || isOverCountOfRetry()) {
            return true;
        }

        // 可以打开条件开关
        return false;
    }

    /**
     * 查log表是否存在：create_time=当天且business_code=707
     * 从当天00:10开始查询，防止日志记录mq跨天消费
     * @return true:是，false：否
     */
    private boolean hasOverCountOfCode() {
        // 获取当天的00:10
        LocalDateTime localDateTime = LocalDate.now().atTime(0, 10, 0);
        Date createTimeStart = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        XieChengCollidingDataLogExample logExample = new XieChengCollidingDataLogExample();
        logExample.createCriteria().andIsDeleteEqualTo(0)
                .andBusinessCodeEqualTo(707)
                .andCreateTimeGreaterThanOrEqualTo(createTimeStart);
        int overCount = logMapper.countByExample(logExample);

        boolean b = overCount > 0;
        if (b) {
            String msg = "携程撞库暂停通知:code返回707";
            service.sendDingDingAlert("携程撞库暂停通知", msg);
            log.error(msg);
        }
        return b;
    }

    /**
     * 查TRUE表当天撞回量级是否超限（500w）
     * @return true:是，false：否
     */
    private boolean isOverCountOfTrue() {
        Integer trueDataThresholdSize = variableAllocationService.getVariableAllocation().getNormalQuantity();

        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(1);
        Date pushTimeStart = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date pushTimeEnd = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // pushTime是当天
        XieChengCollidingDataLoopCycleExample cycleExample = new XieChengCollidingDataLoopCycleExample();
        cycleExample.createCriteria()
                .andIsDeleteEqualTo(0)
                .andPushTimeGreaterThanOrEqualTo(pushTimeStart).andPushTimeLessThan(pushTimeEnd);
        int trueDataCount = loopCycleMapper.countByExample(cycleExample);

        boolean b = trueDataCount >= trueDataThresholdSize;
        if (b) {
            String msg = "携程撞库暂停通知:今天撞得总量级已超过设定阈值：" + trueDataThresholdSize;
            service.sendDingDingAlert("携程撞库暂停通知", msg);
            log.error(msg);
        }
        return b;
    }

    /**
     * 查询堆积量级是否超限（10w）pushTime是当天
     * @return true:是，false：否
     */
    private boolean isOverCountOfRetry() {
        Integer retryThresholdSize = variableAllocationService.getVariableAllocation().getAbnormalQuantity();
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(1);
        Date pushTimeStart = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date pushTimeEnd = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        XieChengCollidingDataLoopCycleExample loopCycleExample = new XieChengCollidingDataLoopCycleExample();
        loopCycleExample.createCriteria().andIsDeleteEqualTo(0)
                .andRetryCountGreaterThan(0)
                .andPushTimeGreaterThanOrEqualTo(pushTimeStart).andPushTimeLessThan(pushTimeEnd);
        int cycleCount = loopCycleMapper.countByExample(loopCycleExample);

        XieChengCollidingDataRobExample robExample = new XieChengCollidingDataRobExample();
        robExample.createCriteria().andIsDeleteEqualTo(0)
                .andRetryCountGreaterThan(0)
                .andPushTimeGreaterThanOrEqualTo(pushTimeStart).andPushTimeLessThan(pushTimeEnd);
        int robCount = robMapper.countByExample(robExample);

        boolean b = cycleCount + robCount >= retryThresholdSize;
        if (b) {
            String msg = "携程撞库暂停通知:今天异常数据堆积总量级已超过设定阈值：" + retryThresholdSize;
            service.sendDingDingAlert("携程撞库暂停通知", msg);
            log.error(msg);
        }
        return b;
    }
}