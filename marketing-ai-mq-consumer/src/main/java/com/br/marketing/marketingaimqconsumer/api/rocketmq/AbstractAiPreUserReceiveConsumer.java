package com.br.marketing.marketingaimqconsumer.api.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.PushRuleService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public abstract class AbstractAiPreUserReceiveConsumer extends BaseMqMessageListener
        implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

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
        log.warn("AiPreUserReceiveConsumer获取消息成功：brokerName[{}]topic[{}]tags[{}]storeTimestamp[{}]msgId[{}]infoId[{}]",
                messageExt.getBrokerName(), messageExt.getTopic(),
                messageExt.getTags(), messageExt.getStoreTimestamp(), messageExt.getMsgId(), id);
        consumerService.consumerRun(messageExt, pushRuleService::insertMarketingPreUserSync, id);
    }

    @Override
    protected void overMaxRetryTimesMessage(MessageExt messageExt) {
        // 可添加自定义处理逻辑
    }

    @Override
    protected boolean isThrowException() {
        return true;
    }

    @Override
    public void onMessage(MessageExt messageExt) {
        super.dispatchMessage(messageExt);
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer defaultMQPushConsumer) {
        defaultMQPushConsumer.setClientRebalance(false);
        defaultMQPushConsumer.setPopInvisibleTime(300000L);
    }
}
