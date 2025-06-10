package com.br.marketing.service.Impl;

import com.alibaba.fastjson2.JSON;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.handle.CachedMessageIdempotentHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.function.Function;

/**
 * 替换原来的ConsumerService
 *
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-07-18
 */
@Slf4j
@Service
public class RocketMqConsumerService {

    @Resource
    private AlarmApiClient alarmClient;

    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private CachedMessageIdempotentHandler cachedMessageIdempotentHandler;

    /**
     * RocketMQ消费端
     *
     * @param messageExt 消费消息
     * @param method     消费业务
     * @param t          消费信息
     * @param retryTag   Tag（消息重试使用）
     *                   使用时去marketing-utils/src/main/java/com/br/marketing/common/constants/rocketmq 包中核对
     * @param delayTopic 消息延时对应的延时队列
     *                   使用时去marketing-utils/src/main/java/com/br/marketing/common/constants/rocketmq 包中核对
     * @param delayTime  消息延时时间（单位：秒） delayTime
     *                   delayTime>0时，发送到延时Topic下
     */
    public <T> void consumerRun(MessageExt messageExt, Function<T, Result<Boolean>> method, T t
            , String delayTopic, String retryTag, long delayTime, boolean checkAndMarkMessageProcessed) {
        try {
            if (ConsumerService.consumerDownStatus) {
                log.warn("服务下线，消费者不在接收新的流量");
                Thread.sleep(12000L);
                log.warn("服务下线，消费者休眠时间到");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("消费者休眠异常", e);
        }
        long startTime = System.currentTimeMillis();
        String uuid = messageExt.getProperty(RocketMqSwitch.UUID_KEY);
        String topic = messageExt.getTopic();
        String tags = messageExt.getTags();
        String msgId = messageExt.getMsgId();
        try {
            if (checkAndMarkMessageProcessed && rocketMqSwitch.msgIdemFlag(tags)
                    && !cachedMessageIdempotentHandler.checkAndMarkMessageProcessed(topic, uuid, msgId)) {
                log.warn("消息重复消费，topic：{},tags：{},msgId：{},uuid：{}", topic, tags, msgId, uuid);
                rocketMqSwitch.rocketLogSwitchFlag(tags, messageExt, t, startTime);
                return;
            }
        } catch (Exception e) {
            log.error("消息去重失败{},消息信息:{}", e.getMessage(), JSON.toJSONString(messageExt), e);
        }
        try {
            Result<Boolean> apply = method.apply(t);
            /*
             * code 为SUCCESS 认为消费成功
             *      根据返回结果来判断是否需要重新推送队列 false-不需要；true需要
             * code 为False 任务消费失败，重推队列
             */
            if (ResultCode.SUCCESS.getValue().equals(apply.getCode())) {
                if (null != apply.getData() && apply.getData()) {
                    if (StringUtils.isNotBlank(delayTopic) && StringUtils.isNotBlank(retryTag)) {
                        if (delayTime > 0) {
                            // 根据消费端配置的[延迟Topic]和[Tags]发送
                            rocketMqSwitch.syncSendDelaySecond(delayTopic, retryTag, t, delayTime);
                        } else {
                            // 根据消费端配置的[普通Topic]和[Tags]发送
                            rocketMqSwitch.syncSend(delayTopic, retryTag, t);
                        }
                    } else {
                        // 消息重新入本队列
                        rocketMqSwitch.syncSend(topic, tags, t);
                    }
                }
            } else {
                String msg = String.format("RocketMQ消息重试topic:%s,Tags：%s,uuid:%s,msgId:%s,message:%s,messageExt:%s"
                        , topic, tags, uuid, msgId, t, messageExt);
                log.warn(msg);
                cachedMessageIdempotentHandler.markMessageProcessFailed(topic, uuid);
                throw new RuntimeException();
            }
        } catch (Exception e) {
            cachedMessageIdempotentHandler.markMessageProcessFailed(topic, uuid);
            String error = String.format("RocketMQ消费异常topic:%s,Tags:%s,uuid:%s,msgId:%s,message:%s，messageExt:%s，\r\n错误信息:%s"
                    , topic, tags, uuid, msgId, t, e.getMessage(), messageExt);
            log.warn(error, e);
            alarmClient.sendAlarm(error, "RocketMQ消费异常", AlarmSendCodeEnum.ROCKETMQ_CONSUMER_ERROR.getCode());
            throw new RuntimeException();
        }
        rocketMqSwitch.rocketLogSwitchFlag(tags, messageExt, t, startTime);
    }

    /**
     * RocketMQ消费端
     *
     * @param messageExt 消费消息
     * @param method     消费业务
     * @param t          消费信息
     * @param <T>        消费消息类型
     */
    public <T> void consumerRun(MessageExt messageExt, Function<T, Result<Boolean>> method, T t
            , String delayTopic, String retryTag, long delayTime) {
        consumerRun(messageExt, method, t, delayTopic, retryTag, delayTime, true);
    }

    /**
     * RocketMQ消费端
     *
     * @param messageExt 消费消息
     * @param method     消费业务
     * @param t          消费信息
     * @param <T>        消费消息类型
     */
    public <T> void consumerRun(MessageExt messageExt, Function<T, Result<Boolean>> method, T t) {
        consumerRun(messageExt, method, t, null, null, 0L, true);
    }

    public <T> void consumerRun(MessageExt messageExt, Function<T, Result<Boolean>> method, T t, boolean checkAndMarkMessageProcessed) {
        consumerRun(messageExt, method, t, null, null, 0L, checkAndMarkMessageProcessed);
    }

    /**
     * pulsar消费端
     *
     * @param subscription 订阅者
     * @param method       消费业务方法
     * @param consumerNum  消费者数量
     * @param topic        主题，死信，重试
     */
    public void consumerPulsar(String subscription, Function<String, Result<Boolean>> method, Integer consumerNum, String... topic) {
        if (consumerNum == null || consumerNum <= 0) {
            consumerNum = 1;
        }
        for (int i = 0; i < consumerNum; i++) {
            new PulsarConsumerThread(method, subscription, topic).start();
        }
    }

}
