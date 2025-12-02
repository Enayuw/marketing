package com.br.marketing.mq.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingCallRecordConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.ZnkfPushService;
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
 * 通话记录版本明细表异步入库消费端
 *
 * @ClassName MarketingCallRecordVersionConsumer
 * @Description 消费MQ消息，异步插入CallRecording记录表
 * @Author kongbx
 * @Date 2025/11/26 13:50
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingCallRecordConstants.TOPIC,
        consumerGroup = MarketingCallRecordConstants.MARKETING_CALL_RECORD_VERSION_INSERT_QUEUE,
        selectorExpression = MarketingCallRecordConstants.TAG_MARKETING_CALL_RECORD_VERSION_INSERT,
        consumeThreadNumber = 1, consumeThreadMax = 1)
public class MarketingCallRecordVersionConsumer extends BaseMqMessageListener
        implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private ZnkfPushService znkfPushService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        try {
            // 将MQ消息体（字符串）转换为Long类型的数据ID
            Long dataId = Long.parseLong(bodyString.trim());
            consumerService.consumerRun(messageExt, (Long message) -> znkfPushService.insertCallRecordingFromMq(message), dataId);
        } catch (NumberFormatException e) {
            log.error("MQ消息体格式错误，无法转换为Long类型，bodyString={}, msgId={}", bodyString, messageExt.getMsgId(), e);
            throw new RuntimeException("MQ消息体格式错误，无法转换为Long类型：" + bodyString, e);
        }
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
