package com.br.marketing.xcconsumer.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.Impl.xc.XieChengReportService;
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

@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_MOCK_DELAY,
        consumerGroup = MarketingXieChengConstants.GROUP_MARKETING_XIECHENG_REPORT_DELAY,
        selectorExpression = MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_MOCK_DELAY,
        consumeThreadNumber = 25, consumeThreadMax = 65, awaitTerminationMillisWhenShutdown = 10000)
public class MarketingXiechengReportMockDelayConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt>,
        RocketMQPushConsumerLifecycleListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    XieChengReportService xieChengReportService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) {
        String bodyString = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        Long o = JSON.parseObject(bodyString, new TypeReference<Long>() {
        }.getType());
        log.warn("MARKETING_XIECHENG_REPORT_MOCK_DELAY_QUEUE" +
                        "：storeTimestamp[{}]msgId[{}]brokerName[{}]topic[{}]tags[{}]获取消息成功:{}"
                , messageExt.getStoreTimestamp(), messageExt.getMsgId()
                , messageExt.getBrokerName(), messageExt.getTopic()
                , messageExt.getTags(), o);
        consumerService.consumerRun(messageExt, xieChengReportService::pushXieChengData, o);
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
