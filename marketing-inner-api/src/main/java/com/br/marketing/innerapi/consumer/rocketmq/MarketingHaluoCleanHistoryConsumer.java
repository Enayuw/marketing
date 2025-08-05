package com.br.marketing.innerapi.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.strategy.HaloCleanHistoryHandler;
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
 * 消费 哈啰数据清洗
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/19 11:33
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingOutsideInterfaceConstants.TOPIC,
        consumerGroup = MarketingOutsideInterfaceConstants.MARKETING_HALUO_CLEAN_HISTORY,
        selectorExpression = MarketingOutsideInterfaceConstants.TAG_MARKETING_HALUO_CLEAN_HISTORY,
        consumeThreadNumber = 1, consumeThreadMax = 1, awaitTerminationMillisWhenShutdown = 3000, consumeTimeout = 30)
public class MarketingHaluoCleanHistoryConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    private HaloCleanHistoryHandler haloCleanHistoryHandler;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        consumerService.consumerRun(messageExt, haloCleanHistoryHandler::haluoCleanHistory, bodyString);
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
