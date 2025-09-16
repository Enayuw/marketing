package com.br.marketing.marketingaidatapushdown.api.rocketmq;

import com.br.marketing.common.constants.rocketmq.AiRocketMQConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.strategy.InterfaceHandlerService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * hong.chen
 * 2025/09/04
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = AiRocketMQConstants.TOPIC_MARKETING_AI_UNIVERSAL_RECEIVE_BATCH,
        consumerGroup = AiRocketMQConstants.GROUP_MARKETING_AI_UNIVERSAL_RECEIVE_BATCH,
        selectorExpression = AiRocketMQConstants.TAG_MARKETING_AI_UNIVERSAL_RECEIVE_BATCH,
        consumeThreadNumber = 5, consumeThreadMax = 20, awaitTerminationMillisWhenShutdown = 2000)
public class AiUniversalReceiveBatchConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt>,
        RocketMQPushConsumerLifecycleListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private InterfaceHandlerService interfaceHandlerService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) {
        String bodyString = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.warn("AiUniversalReceiveBatchConsumer获取消息成功：brokerName[{}]topic[{}]tags[{}]storeTimestamp[{}]msgId[{}]bodyString[{}]",
                messageExt.getBrokerName(), messageExt.getTopic(),
                messageExt.getTags(), messageExt.getStoreTimestamp(), messageExt.getMsgId(), bodyString);
        consumerService.consumerRun(messageExt, interfaceHandlerService::handleDataDirection, bodyString);
    }

    @Override
    protected void overMaxRetryTimesMessage(MessageExt messageExt) {

    }

    @Override
    protected boolean isThrowException() {
        // true会重新消费消息
        return true;
    }

    @Override
    public void onMessage(MessageExt messageExt) {
        super.dispatchMessage(messageExt);
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer defaultMQPushConsumer) {
        defaultMQPushConsumer.setPullBatchSize(5);
        defaultMQPushConsumer.setPopBatchNums(5);
    }
}
