package com.br.marketing.service.Impl.ai;

import com.br.common.log.AlertLog;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.Impl.ConsumerService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

/**
 * @Description AiConsumerServiceImpl
 * @Author hong.chen
 * @CreateTime 2025/04/11
 */
@Service
public class AiConsumerService {
    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);
    @Autowired
    private RabbitMqProducter producter;

    public static Boolean consumerDownStatus = Boolean.FALSE;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    ConsumerService consumerService;

    public <T> void consumerWithThreadPool(Channel channel, Message message, Function<T, Result<Boolean>> method, T t, String exRouteKey,
                                           String queueType, ThreadPoolExecutor poolExecutor) {
        // 下线标识，不再消费消息
        if (consumerDownStatus) {
            log.warn("服务下线，消费者不再接收新的流量");
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage()
                        , "mq消费端，服务下线，线程休眠异常"), e);
                Thread.currentThread().interrupt();
            }
            log.warn("服务下线，消费者休眠时间到");
        }

        poolExecutor.setCorePoolSize(marketingCommonConfig.getAiMqConsumerCommonThreadNum());
        poolExecutor.setMaximumPoolSize(marketingCommonConfig.getAiMqConsumerCommonThreadNum());
        poolExecutor.submit(() -> {
            try {
                Result<Boolean> apply = method.apply(t);
                if (ResultCode.FAIL.getValue().equals(apply.getCode()) || apply.getData()) {
                    producter.send(exRouteKey, new String(message.getBody(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                String error = String.format("路由键：%s,\r\n消息内容：%s,\r\n错误信息：%s"
                        , message.getMessageProperties().getReceivedRoutingKey()
                        , new String(message.getBody(), StandardCharsets.UTF_8)
                        , e.getMessage());
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), error
                        , "mq消费端，消费异常，消息发送到异常重试队列"), e);
                try {
                    producter.send(exRouteKey, new String(message.getBody(), StandardCharsets.UTF_8));
                } catch (Exception ee) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage()
                            , "mq消费端，消息发送到异常重试队列异常"), e);
                }
            }
        });

        cacheMsgCount(message, queueType);

        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage()
                    , "mq消费端，消息ack异常"), e);
        }
    }

    /**
     * 缓存当前队列的消息数量到redis
     * @param message
     * @param queueType
     */
    private void cacheMsgCount(Message message, String queueType) {
        try {
            MessageProperties messageProperties = message.getMessageProperties();
            String routingKey = messageProperties.getReceivedRoutingKey();
            int currentMsgCount = messageProperties.getMessageCount();
            redisChgService.zadd(RedisKeyConstant.prefix.concat(queueType), routingKey, (long) currentMsgCount);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage()
                    , "mq消费端，缓存当前队列的消息异常"), e);
        }
    }

    public <T> void consumerWithOutThreadPool(Channel channel, Message message, Function<T, Result<Boolean>> method, T t, String retryRouteKey,
                                              String queueType) {
        try {
            /**
             * 下线标识，不在消费消息
             */
            if (consumerDownStatus) {
                log.warn("服务下线，消费者不在接收新的流量");
                Thread.sleep(10000L);
                log.warn("服务下线，消费者休眠时间到");
            }

            Result<Boolean> apply = method.apply(t);

            cacheMsgCount(message, queueType);
            /**
             * code 为SUCCESS 认为消费成功
             *      根据返回结果来判断是否需要重新推送队列 false-不需要；true需要
             * code 为False 任务消费失败，重推队列
             */
            if (ResultCode.SUCCESS.getValue().equals(apply.getCode())) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                if (apply.getData()) {
                    if (StringUtils.isNotBlank(retryRouteKey)) {
                        producter.send(retryRouteKey, new String(message.getBody(), StandardCharsets.UTF_8));
                    } else {
                        producter.send(message.getMessageProperties().getReceivedRoutingKey(), new String(message.getBody(), StandardCharsets.UTF_8));
                    }
                }
            } else {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        } catch (Exception e) {
            String error = String.format("路由键：%s,\r\n消息内容：%s,\r\n错误信息：%s"
                    , message.getMessageProperties().getReceivedRoutingKey()
                    , new String(message.getBody(), StandardCharsets.UTF_8)
                    , e.getMessage());
            log.error(error, e);
            alarmClient.sendAlarm(error, "消费异常", AlarmSendCodeEnum.ERROR_UNKNOWN.getCode());
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public <T> void consumerAndCacheMsgCount(Channel channel, Message message, Function<T, Result<Boolean>> method, T t, String exRouteKey,
                                             String queueType, ThreadPoolExecutor poolExecutor) {
        if (marketingCommonConfig.getAiMqEnableThreadPoolSwitch()) {
            consumerWithThreadPool(channel, message, method, t, exRouteKey, queueType, poolExecutor);
        } else {
            consumerWithOutThreadPool(channel, message, method, t, exRouteKey, queueType);
        }
    }
}
