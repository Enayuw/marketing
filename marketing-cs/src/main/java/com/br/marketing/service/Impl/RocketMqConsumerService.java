package com.br.marketing.service.Impl;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.context.MqIdempotentContext;
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

    /**
     * RocketMQ消费端 重试、延时队列、消息幂等、服务异常退出后，消费消息的重试机制
     *
     * @param messageExt            消息体
     * @param method                业务
     * @param t                     信息
     * @param retryTag              Tag（消息重试使用）
     *                              使用时去marketing-utils/src/main/java/com/br/marketing/common/constants/rocketmq 包中核对
     * @param delayTopic            消息延时对应的延时队列
     *                              使用时去marketing-utils/src/main/java/com/br/marketing/common/constants/rocketmq 包中核对
     * @param delayTime             消息延时时间（单位：秒） delayTime
     *                              delayTime>0时，发送到延时Topic下
     */
    public <T> void consumerRun(MessageExt messageExt, Function<T, Result<Boolean>> method, T t
            , String delayTopic, String retryTag, long delayTime) {
        long startTime = System.currentTimeMillis();
        String keys = messageExt.getKeys();
        String topic = messageExt.getTopic();
        String tags = messageExt.getTags();
        String msgId = messageExt.getMsgId();
        Result<Boolean> apply = null;
        try {
            // 设置tag到ThreadLocal，供幂等性切面使用
            MqIdempotentContext.setTag(tags);
            apply = method.apply(t);
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
                String msg = String.format("RocketMQ消息重试t,Result:%s, topic:%s,Tags:%s,keys:%s,msgId:%s,message:%s,messageExt:%s"
                        , apply, topic, tags, keys, msgId, t, messageExt);
                log.warn(msg);
                throw new RuntimeException(msg);
            }
        } catch (Exception e) {
            String error = String.format("RocketMQ消费异常: %s \r\ntopic:%s,Tags:%s,keys:%s,msgId:%s,message:%s,messageExt:%s,Result:%s"
                    , e.getMessage(), topic, tags, keys, msgId, t, messageExt, apply == null ? "null" : apply.toString());
            log.warn(error, e);
            alarmClient.sendAlarm(error, "RocketMQ消费异常", AlarmSendCodeEnum.ROCKETMQ_CONSUMER_ERROR.getCode());
            throw new RuntimeException(error);
        } finally {
            // 清理ThreadLocal
            MqIdempotentContext.clear();
        }
        rocketMqSwitch.rocketLogSwitchFlag(tags, messageExt, t, startTime);
    }

    /**
     * RocketMQ消费端，消息幂等，支持重复消费时间
     *
     * @param messageExt            消息体
     * @param method                业务
     * @param t                     信息
     */
    public <T> void consumerRun(MessageExt messageExt, Function<T, Result<Boolean>> method, T t) {
        consumerRun(messageExt, method, t, null, null, 0L);
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
