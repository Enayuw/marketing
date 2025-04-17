package com.br.marketing.marketingaimqconsumer.job;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SwitchMessageQueueEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description DetermineTargetMessageQueueJob
 * @Author hong.chen
 * @CreateTime 2025/04/17
 */
@Component
@Slf4j
public class DetermineTargetMessageQueueJob extends AbstractSimpleElasticJob {
    @Autowired
    RedisChgService redisChgService;

    @Qualifier("connectionFactoryChannel")
    @Autowired
    Channel channel;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    TpDynamicExecutor threadPoolExecutor = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.SWITCH_MESSAGE_QUEUE.getName(), 10,
            10);

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

        for (SwitchMessageQueueEnum switchMessageQueueEnum : SwitchMessageQueueEnum.values()) {
            threadPoolExecutor.submit(() -> {
                try {
                    String currentRoutingKey = redisChgService.hget(RedisKeyConstant.SWITCH_MESSAGE_QUEUE,
                            switchMessageQueueEnum.name());

                    if (StringUtils.isEmpty(currentRoutingKey)) {
                        redisChgService.hset(RedisKeyConstant.SWITCH_MESSAGE_QUEUE, switchMessageQueueEnum.name(),
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

                    String winnerRoutingKey =
                            routingKeyAndMsgCountMap.entrySet().stream()
                                    .min(Comparator.comparingInt(Map.Entry::getValue))
                                    .map(Map.Entry::getKey).orElse(currentRoutingKey);

                    if (currentRoutingKey.equals(winnerRoutingKey)) {
                        return;
                    }

                    redisChgService.hset(RedisKeyConstant.SWITCH_MESSAGE_QUEUE, switchMessageQueueEnum.name(), winnerRoutingKey);
                    log.warn("当前消费队列路由键：{}，切换到最小压力队列路由键：{}", currentRoutingKey, winnerRoutingKey);
                } catch (IOException e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage()
                            , "DetermineTargetMessageQueueJob，队列切换异常"), e);
                }
            });
        }
    }
}
