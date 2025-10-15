package com.br.marketing.service.Impl;

import java.util.Map;
import java.util.function.Function;

import com.br.arch.geo.pulsar.encrypt.PulsarEncryptUtil;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Messages;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;

import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarConsumer;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.spring.ContainerContext;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PulsarConsumerThread extends Thread {

    /**
     * 消费业务代码
     */
    Function<String, Result<Boolean>> method;

    /**
     * 主题
     */
    String topic;

    /**
     * 死信
     */
    String dealLine;

    /**
     * 重试
     */
    String retry;

    /**
     * 订阅者
     */
    String subscription;

    public PulsarConsumerThread(Function<String, Result<Boolean>> method, String subscription, String... topic) {
        this.method = method;
        this.subscription = subscription;
        this.topic = topic[0];
        if (topic.length > 1) {
            this.dealLine = topic[1];
        }
        if (topic.length > 2) {
            this.retry = topic[2];
        }
    }

    @Override
    public void run() {
        try {
            ProductPulsarConsumer consumer = null;
            if (StringUtils.isNotBlank(this.dealLine) && StringUtils.isNotBlank(this.retry)) {
                consumer = ProductPulsarClientManager.newConsumer(topic, dealLine, retry, subscription, SubscriptionType.Shared);
            } else {
                consumer = ProductPulsarClientManager.newConsumer(topic, subscription, SubscriptionType.Shared);
            }
            log.warn("ProductPulsarConsumer 初始化成功,method:{},topic:{},dealLine:{},retry:{},subscription:{}", method.toString(), topic, dealLine, retry,
                subscription);
            while (true) {
                Map<String, MarketingCommonConfig> beansOfType = ContainerContext.applicationContext.getBeansOfType(MarketingCommonConfig.class);
                if (beansOfType != null) {
                    MarketingCommonConfig marketingCommonConfig = beansOfType.get("marketingCommonConfig");
                    if (marketingCommonConfig.getPulsarSwitch() != null && marketingCommonConfig.getPulsarSwitch()) {
                        try {
                            Thread.sleep(5000L);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                        continue;
                    }
                }
                Messages<byte[]> messages = consumer.batchReceive();
                for (Message<byte[]> message : messages) {
                    Boolean isAck = Boolean.FALSE;
                    //消息解密
                    String messageData = PulsarEncryptUtil.decrypt(message.getData(), message.getProperties());
                    if (message.isReplicated()) {
                        isAck = Boolean.TRUE;
                        log.warn(String.format("pulsar接收异地机房消息,topic【%s】，message【%s】", topic, messageData));
                    } else {
                        try {
                            Result<Boolean> apply = method.apply(messageData);
                            if (ResultCode.SUCCESS.getValue().equals(apply.getCode())) {
                                isAck = Boolean.TRUE;
                            }
                        } catch (Exception ex) {
                            log.error(String.format("pulsar消费异常,topic【%s】，message【%s】", topic, messageData), ex);
                        }
                    }
                    if (isAck) {
                        consumer.ack(message.getMessageId());
                    } else {
                        consumer.nack(message.getMessageId());
                    }
                }
            }
        } catch (PulsarClientException ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
