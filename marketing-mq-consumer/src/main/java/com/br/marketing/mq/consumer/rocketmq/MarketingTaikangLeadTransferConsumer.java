package com.br.marketing.mq.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.TaikangLeadTransferService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.stereotype.Service;

/**
 * 泰康大健康线索线索传输消费端
 *
 * @author senyang.zheng
 * @date 2025/11/24
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingAssistConstants.TOPIC,
        consumerGroup = MarketingAssistConstants.MARKETING_TAIKANG_LEAD_TRANSFER_QUEUE,
        selectorExpression = MarketingAssistConstants.TAG_MARKETING_TAIKANG_LEAD_TRANSFER,
        consumeThreadNumber = 1, consumeThreadMax = 1, awaitTerminationMillisWhenShutdown = 5000)
public class MarketingTaikangLeadTransferConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Resource
    private RocketMqConsumerService consumerService;

    @Resource
    private TaikangLeadTransferService taikangLeadTransferService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        consumerService.consumerRun(messageExt, taikangLeadTransferService::transferData, bodyString);
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
