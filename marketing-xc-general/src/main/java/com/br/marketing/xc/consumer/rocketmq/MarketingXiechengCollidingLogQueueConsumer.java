package com.br.marketing.xc.consumer.rocketmq;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.Impl.xc.XieChengCollidingDataLogService;
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
import java.util.List;

/**
 * 营销携程撞库日志消息消费端
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/20 20:57
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingXieChengConstants.TOPIC,
        consumerGroup = MarketingXieChengConstants.MARKETING_XIECHENG_COLLIDING_LOG_QUEUE,
        selectorExpression = MarketingXieChengConstants.TAG_MARKETING_XIECHENG_COLLIDING_LOG_QUEUE,
        consumeThreadNumber = 2, consumeThreadMax = 10)
public class MarketingXiechengCollidingLogQueueConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    RocketMqConsumerService consumerService;
    @Resource
    private XieChengCollidingDataLogService xieChengCollidingDataLogService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        List<XieChengCollidingDataLog> collidingDataLogList = JSONArray.parseArray(bodyString, XieChengCollidingDataLog.class);
        consumerService.consumerRun(messageExt, xieChengCollidingDataLogService::saveXieChengCollidingDataLog, collidingDataLogList);
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
        defaultMQPushConsumer.setPullBatchSize(2);
        defaultMQPushConsumer.setPopBatchNums(2);
    }

}
