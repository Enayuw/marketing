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
import cn.hutool.core.lang.Pair;
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
 * 技术方案地址：https://c.100credit.cn/pages/viewpage.action?pageId=151477608
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
        if (marketingCommonConfig.getXieChengForceOpenSwitch() || canOpenConditionSwitchAndExecute()) {
            // 执行重试撞库
            service.process();
        }
        log.warn("携程异常重试作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }

    /**
     * 获取撞库暂停code码和暂停通知
     * code码含义：0:需要开启条件开关，-1和1~3:需要关闭条件开关
     * -1:返回707且已发送过告警，1:返回707且未发送过告警，2:撞得量级超限，3:重试堆积量级超限
     */
    private Pair<Integer, String> getCodeAndMsg() {
        // 查log表是否存在：create_time=当天且business_code=707
        // 从当天01:00开始查询，防止日志记录mq跨天消费
        LocalDateTime localDateTime = LocalDate.now().atTime(1, 0, 0);
        Date createTimeStart = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        XieChengCollidingDataLogExample logExample = new XieChengCollidingDataLogExample();
        logExample.createCriteria().andIsDeleteEqualTo(0)
                .andBusinessCodeEqualTo(707)
                .andCreateTimeGreaterThanOrEqualTo(createTimeStart);
        int overCount = logMapper.countByExample(logExample);
        boolean a = overCount > 0;
        if (a) {
            // 查log表是否存在：create_time=当天且business_code=707且extend.isAlerted=true
            XieChengCollidingDataLog alertedLog = logMapper.selectByOverCountAlerted(createTimeStart);

            // 未发送过钉钉告警：设置isAlert，发钉钉
            if (alertedLog == null) {
                XieChengCollidingDataLog dataLog = logMapper.selectByOverCount(createTimeStart);
                updateByAlert(dataLog);
                String msg = "携程撞库暂停通知：撞满撞库额度";
                return new Pair<>(1, msg);
            }

            // 已发过707钉钉告警：关闭条件开关
            return new Pair<>(-1, null);
        }

        // 查TRUE表当天撞回量级是否超限（500w）
        Integer trueDataThresholdSize = variableAllocationService.getVariableAllocation().getNormalQuantity();
        if (trueDataThresholdSize == null) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                    , "携程异常数据重试撞库,获取当天撞回阈值失败"));
            return new Pair<>(-2, "获取当天撞回阈值失败");
        }
        LocalDate start = LocalDate.now().plusDays(7);
        LocalDate end = LocalDate.now().plusDays(8);
        Date releaseDateStart = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date releaseDateEnd = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());
        // release_time大于第七天，小于第八天
        XieChengCollidingDataLoopCycleExample cycleExample = new XieChengCollidingDataLoopCycleExample();
        cycleExample.createCriteria()
                .andIsDeleteEqualTo(0)
                .andReleaseTimeGreaterThanOrEqualTo(releaseDateStart).andReleaseTimeLessThan(releaseDateEnd);
        int trueDataCount = loopCycleMapper.countByExample(cycleExample);
        boolean b = trueDataCount >= trueDataThresholdSize;
        if (b) {
            String msg = "携程撞库暂停通知：今天撞得总量级：" + trueDataCount + "，已达到设定阈值：" + trueDataThresholdSize;
            return new Pair<>(2, msg);
        }

        // 查询堆积量级是否超限（10w）pushTime是当天
        Integer retryThresholdSize = variableAllocationService.getVariableAllocation().getAbnormalQuantity();
        if (retryThresholdSize == null) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                    , "携程异常数据重试撞库,获取当天异常堆积阈值失败"));
            return new Pair<>(-2, "携程异常数据重试撞库暂停通知:获取当天异常堆积阈值失败");
        }
        LocalDate pushDateStart = LocalDate.now();
        LocalDate pushDateEnd = LocalDate.now().plusDays(1);
        Date pushTimeStart = Date.from(pushDateStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date pushTimeEnd = Date.from(pushDateEnd.atStartOfDay(ZoneId.systemDefault()).toInstant());
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
        int count = cycleCount + robCount;
        boolean c = count >= retryThresholdSize;
        if (c) {
            String msg = "携程撞库暂停通知：今天异常数据堆积总量级：" + count + "，已达到设定阈值：" + retryThresholdSize + "，需要手动调整异常量级才能恢复撞库。调整入口：携程定制页面 -> 量级控制 -> 异常报警量级";
            return new Pair<>(3, msg);
        }

        return new Pair<>(0, null);
    }

    /**
     * 发钉钉告警前更新extend.isAlerted=true
     * @param alertedLog
     */
    private void updateByAlert(XieChengCollidingDataLog alertedLog) {
        XieChengCollidingDataLog log = new XieChengCollidingDataLog();
        log.setId(alertedLog.getId());
        JSONObject json = new JSONObject();
        json.put("isAlerted", true);
        log.setExtend(json.toJSONString());
        logMapper.updateByPrimaryKeySelective(log);
    }

    /**
     * 校验是否需要开启条件开关并执行撞库
     * @return true:是，false：否
     */
    private boolean canOpenConditionSwitchAndExecute() {
        String redisSwitch;
        try {
            redisSwitch = redisChgService.get(RedisKeyConstant.XIECHENG_CONDITIONSWITCH);
            log.warn("携程异常数据重试撞库，条件开关状态：{}", redisSwitch);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程异常数据重试撞库，获取redis条件开关失败"), e);
            return false;
        }

        if (StringUtils.isEmpty(redisSwitch)) {
            log.warn("携程异常重试作业，重置条件开关状态为开启");
            redisChgService.set(RedisKeyConstant.XIECHENG_CONDITIONSWITCH, "true");
            return true;
        }

        Pair<Integer, String> pair = getCodeAndMsg();
        Integer code = pair.getKey();
        String msg = pair.getValue();
        if (code == -2) {
            // 钉钉告警
            service.sendDingDingAlert("携程异常数据重试撞库暂停通知", msg);
            return false;
        }

        if (code == -1) {
            // 已发送过707告警：关闭条件开关
            xieChengServiceNew.shutDownConditionSwitch();
            return false;
        }

        if (code == 1) {
            // 未发送过707告警：关闭条件开关并发送钉钉告警
            shutDownConditionSwitchAndAlert(msg);
            return false;
        }

        if ("false".equalsIgnoreCase(redisSwitch) && code == 0) {
            // 开关是关闭状态且需要开启条件开关
            redisChgService.set(RedisKeyConstant.XIECHENG_CONDITIONSWITCH, "true");
            return true;
        } else if ("true".equalsIgnoreCase(redisSwitch) && code > 1) {
            // 开关是开启状态且需要关闭条件开关
            shutDownConditionSwitchAndAlert(msg);
            return false;
        } else {
            return code == 0;
        }
    }

    /**
     * 关闭条件开关并发送钉钉告警
     * @param msg
     */
    private void shutDownConditionSwitchAndAlert(String msg) {
        // 关闭条件开关
        xieChengServiceNew.shutDownConditionSwitch();
        // 钉钉告警
        service.sendDingDingAlertByAtSomeBody(msg);
    }
}