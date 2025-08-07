package com.br.marketing.xcconsumer.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.dto.xiecheng.XieChengActivateDTO;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
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
 * 消费 携程促活数据接入消费端
 * @Author: zhiyong.zhang
 * @Date: 2025-08-06
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_ACTIVE_COLLIDING_QUEUE,
        consumerGroup = MarketingXieChengConstants.GROUP_MARKETING_XIECHENG_ACTIVE_COLLIDING_QUEUE,
        selectorExpression = MarketingXieChengConstants.TAG_MARKETING_XIECHENG_ACTIVE_COLLIDING_QUEUE,
        consumeThreadNumber = 1, consumeThreadMax = 5)
public class MarketingXieChengCollidingActivateConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private XieChengRobDataCollidingService robDataCollidingService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        XieChengActivateDTO xieChengActivateDTO = JSON.parseObject(bodyString,
                new TypeReference<XieChengActivateDTO>() {
                }.getType());
        consumerService.consumerRun(messageExt, robDataCollidingService::activateDataHandle, xieChengActivateDTO);
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
        defaultMQPushConsumer.setClientRebalance(false);
        defaultMQPushConsumer.setPopInvisibleTime(300000L);
    }
}
