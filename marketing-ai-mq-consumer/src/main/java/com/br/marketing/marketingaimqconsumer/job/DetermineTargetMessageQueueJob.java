package com.br.marketing.marketingaimqconsumer.job;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SwitchMessageQueueEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 动态切换消息队列作业
 * @Author hong.chen
 * @CreateTime 2025/04/17
 */
@Component
@Slf4j
public class DetermineTargetMessageQueueJob {
    @Autowired
    private RedisChgService redisChgService;

    @Autowired
    @Qualifier("clusterEnvironment")
    private String clusterEnvironment;

    @Qualifier("connectionFactoryChannel")
    @Autowired
    private Channel channel;

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    private final TpDynamicExecutor threadPoolExecutor = TpDynamicExecutorFactory.getThreadPool(
            ThreadPoolNameEnum.SWITCH_MESSAGE_QUEUE.getName(), 10, 10);

    @Scheduled(cron = "0 0/1 * * * ?")
    public void executeTask() {
        long start = System.currentTimeMillis();
        // 增加开关控制逻辑
        if (!marketingCommonConfig.getIsEnableMqSwitch()) {
            log.warn("动态切换消息队列任务开关已关闭，跳过执行");
            return;
        }

        String redisKey = RedisKeyConstant.SWITCH_MESSAGE_QUEUE + ":" + clusterEnvironment;
        log.warn("动态切换消息队列作业,当前环境为: {}，使用Redis Key: {}", clusterEnvironment, redisKey);

        for (SwitchMessageQueueEnum switchMessageQueueEnum : SwitchMessageQueueEnum.values()) {
            threadPoolExecutor.submit(() -> processQueueSwitch(switchMessageQueueEnum, redisKey, clusterEnvironment));
        }

        log.warn("动态切换消息队列作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }

    private void processQueueSwitch(SwitchMessageQueueEnum switchMessageQueueEnum, String redisKey, String environment) {
        try {
            String currentRoutingKey = redisChgService.hget(redisKey, switchMessageQueueEnum.name());

            if (StringUtils.isEmpty(currentRoutingKey)) {
                redisChgService.hset(redisKey, switchMessageQueueEnum.name(),
                        switchMessageQueueEnum.getDefault_route_key());
                return;
            }

            String currentQueueName = switchMessageQueueEnum.getQueueAndRoutingKeyMap().get(currentRoutingKey);
            AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(currentQueueName);
            int currentMsgCount = declareOk.getMessageCount();
            if (currentMsgCount <= marketingCommonConfig.getSwitchMqMaxMsgCount()) {
                return;
            }

            Map<String, Integer> routingKeyAndMsgCountMap = new HashMap<>();
            Map<String, String> queueAndRoutingKeyMap = switchMessageQueueEnum.getQueueAndRoutingKeyMap();
            for (String key : queueAndRoutingKeyMap.keySet()) {
                routingKeyAndMsgCountMap.put(key, channel.queueDeclarePassive(queueAndRoutingKeyMap.get(key)).getMessageCount());
            }

            log.warn("动态切换消息队列作业[{}]，各队列消息积压情况：{}", environment, JSON.toJSONString(routingKeyAndMsgCountMap));
            String winnerRoutingKey =
                    routingKeyAndMsgCountMap.entrySet().stream()
                            .min(Comparator.comparingInt(Map.Entry::getValue))
                            .map(Map.Entry::getKey).orElse(currentRoutingKey);

            if (currentRoutingKey.equals(winnerRoutingKey)) {
                return;
            }

            redisChgService.hset(redisKey, switchMessageQueueEnum.name(), winnerRoutingKey);
            log.warn("动态切换消息队列作业[{}]，当前消费队列路由键：{}，切换到最小压力队列路由键：{}",
                    environment, currentRoutingKey, winnerRoutingKey);
        } catch (IOException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage(),
                    "动态切换消息队列作业[" + environment + "]，队列切换异常"), e);
        }
    }
}
