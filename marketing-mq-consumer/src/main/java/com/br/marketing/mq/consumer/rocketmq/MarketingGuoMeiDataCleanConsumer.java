package com.br.marketing.mq.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.constants.rocketmq.MarketingUploadConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.clean.guomei.GuoMeiDataCleanService;
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
 * 消费 国美上传数据清洗消费端
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-10-24
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingUploadConstants.TOPIC,
        consumerGroup = MarketingUploadConstants.MARKETING_GUOMEI_DATA_CLEAN,
        selectorExpression = MarketingUploadConstants.TAG_MARKETING_GUOMEI_DATA_CLEAN,
        consumeThreadNumber = 1, consumeThreadMax = 5, awaitTerminationMillisWhenShutdown = 2000)
public class MarketingGuoMeiDataCleanConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    RocketMqConsumerService consumerService;
    @Resource
    private GuoMeiDataCleanService guoMeiDataCleanService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        consumerService.consumerRun(messageExt, guoMeiDataCleanService::cleanData, bodyString);
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
        defaultMQPushConsumer.setPullBatchSize(5);
        defaultMQPushConsumer.setPopBatchNums(5);
    }
}
