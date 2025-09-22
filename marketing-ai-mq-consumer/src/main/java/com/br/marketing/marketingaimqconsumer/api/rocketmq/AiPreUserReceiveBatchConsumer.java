package com.br.marketing.marketingaimqconsumer.api.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.AiRocketMQConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.PushRuleService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * @Author hong.chen
 * @Date 2025/09/04 20:57
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = AiRocketMQConstants.TOPIC_MARKETING_AI_PREUSER_RECEIVE_BATCH,
        consumerGroup = AiRocketMQConstants.GROUP_MARKETING_AI_PREUSER_RECEIVE_BATCH,
        selectorExpression = AiRocketMQConstants.TAG_MARKETING_AI_PREUSER_RECEIVE_BATCH,
        consumeThreadNumber = 5, consumeThreadMax = 10, awaitTerminationMillisWhenShutdown = 5000)
public class AiPreUserReceiveBatchConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt>,
        RocketMQPushConsumerLifecycleListener {

    @Autowired
    protected RocketMqConsumerService consumerService;

    @Autowired
    PushRuleService pushRuleService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) {
        String bodyString = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        Long id = JSON.parseObject(bodyString, new TypeReference<Long>() {
        }.getType());
        log.warn("AiPreUserReceiveBatchConsumer获取消息成功：brokerName[{}]topic[{}]tags[{}]storeTimestamp[{}]msgId[{}]infoId[{}]",
                messageExt.getBrokerName(), messageExt.getTopic(),
                messageExt.getTags(), messageExt.getStoreTimestamp(), messageExt.getMsgId(), id);
        consumerService.consumerRun(messageExt, pushRuleService::insertMarketingPreUserSync, id);
    }

    @Override
    protected void overMaxRetryTimesMessage(MessageExt messageExt) {
        log.warn("overMaxRetryTimes messageExt is [{}]", JSON.toJSONString(messageExt));
    }

    @Override
    protected boolean isThrowException() {
        log.warn("messageExt ThrowException");
        // true会重新消费消息
        return true;
    }

    @Override
    public void onMessage(MessageExt messageExt) {
        super.dispatchMessage(messageExt);
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer defaultMQPushConsumer) {
        defaultMQPushConsumer.setPullBatchSize(1);
        defaultMQPushConsumer.setPopBatchNums(1);
    }
}
