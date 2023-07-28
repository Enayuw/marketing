package com.br.marketing.service.Impl;

import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarConsumer;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Messages;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;

import java.util.function.Function;

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
     * 分区
     */
    String subscription;


    public PulsarConsumerThread(Function<String, Result<Boolean>> method, String subscription, String... topic) {
        this.method = method;
        this.subscription = subscription;
        this.topic = topic[0];
        if(topic.length>1){
            this.dealLine = topic[1];
        }
        if(topic.length>2){
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
            while (true) {
                Messages<byte[]> messages = consumer.batchReceive();
                for (Message<byte[]> message : messages) {
                    Boolean isAck = Boolean.FALSE;
                    String messageData = new String(message.getData());
                    if (message.isReplicated()) {
                        isAck = Boolean.TRUE;
                        log.warn(String.format("pulsar接收异地机房消息,topic【%s】，message【%s】", topic, messageData));
                    }else{
                        try {
                            Result<Boolean> apply = method.apply(messageData);
                            if (ResultCode.SUCCESS.getValue().equals(apply.getCode())) {
                                isAck = Boolean.TRUE;
                            }
                        } catch (Exception ex) {
                            log.error(String.format("pulsar消费异常,topic【%s】，message【%s】", topic, messageData));
                        }
                    }
                    if(isAck){
                        consumer.ack(message.getMessageId());
                    }else{
                        consumer.nack(message.getMessageId());
                    }
                }
            }
        } catch (PulsarClientException ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
