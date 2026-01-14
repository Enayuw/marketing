package com.br.marketing.innerapi.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
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
 * 消费 黑名单
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/19 11:33
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingOutsideInterfaceConstants.TOPIC,
        consumerGroup = MarketingOutsideInterfaceConstants.MARKETING_TRANSFER_PUSH_BLACK,
        selectorExpression = MarketingOutsideInterfaceConstants.TAG_MARKETING_TRANSFER_PUSH_BLACK,
        consumeThreadNumber = 1, consumeThreadMax = 2, awaitTerminationMillisWhenShutdown = 2000)
public class MarketingTransferPushBlackConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    PushRuleService pushRuleService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
//        Long o = JSON.parseObject(bodyString, new TypeReference<Long>() {}.getType());
        consumerService.consumerRun(messageExt, pushRuleService::consumerBlack, bodyString);
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
        defaultMQPushConsumer.setPullBatchSize(1);
        defaultMQPushConsumer.setPopBatchNums(1);
    }

}
