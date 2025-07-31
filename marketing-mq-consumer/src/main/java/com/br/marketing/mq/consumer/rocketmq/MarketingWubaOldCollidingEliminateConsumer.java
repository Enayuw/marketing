package com.br.marketing.mq.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingWuBaConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.Impl.wuba.WuBaOldCollidingDataQueryResultService;
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
 * 58旧撞库数据消费
 *
 * @Author: Hua Qiang
 * @Date: 2025-05-28
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingWuBaConstants.TOPIC,
        consumerGroup = MarketingWuBaConstants.MARKETING_WUBA_OLD_COLLIDING_ELIMINATE_QUEUE,
        selectorExpression = MarketingWuBaConstants.TAG_MARKETING_WUBA_OLD_COLLIDING_ELIMINATE,
        consumeThreadNumber = 1, consumeThreadMax = 5)
public class MarketingWubaOldCollidingEliminateConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private WuBaOldCollidingDataQueryResultService wuBaOldCollidingDataQueryResultService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        consumerService.consumerRun(messageExt, wuBaOldCollidingDataQueryResultService::buildEliminateAndPushToRobot, bodyString);
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
