package com.br.marketing.xcconsumer.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.XieChengSmsPushToTransferService;
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
 * @Author hong.chen
 * @Date 2025/06/30 20:57
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingXieChengConstants.CPS_PUSH_ROBOT_TOPIC,
        consumerGroup = MarketingXieChengConstants.GROUP_MARKETING_XIECHENG_CPS_PUSH_ROBOT_QUEUE,
        selectorExpression = MarketingXieChengConstants.TAG_MARKETING_XIECHENG_CPS_PUSH_ROBOT,
        consumeThreadNumber = 5, consumeThreadMax = 10, awaitTerminationMillisWhenShutdown = 10000)
public class MarketingXieChengCpsPushRobotConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> , RocketMQPushConsumerLifecycleListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    XieChengSmsPushToTransferService xieChengSmsPushToTransferService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) {
        String bodyString = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.warn("Marketing_XieCheng_Cps_Push_Robot_Queue：" +
                        "storeTimestamp[{}]msgId[{}]brokerName[{}]topic[{}]tags[{}]获取消息成功:{}"
                , messageExt.getStoreTimestamp(), messageExt.getMsgId()
                , messageExt.getBrokerName(), messageExt.getTopic()
                , messageExt.getTags(), bodyString);
        consumerService.consumerRun(messageExt, xieChengSmsPushToTransferService::consumerXiechengSmsCollidingVtUser, bodyString);
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
        defaultMQPushConsumer.setClientRebalance(false);
        defaultMQPushConsumer.setPopInvisibleTime(300000L);
    }
}
