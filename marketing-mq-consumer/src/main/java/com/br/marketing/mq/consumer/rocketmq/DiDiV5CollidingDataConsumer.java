package com.br.marketing.mq.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.didi.DiDiCollidingDataService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingOutsideInterfaceConstants.TOPIC,
        consumerGroup = MarketingOutsideInterfaceConstants.MARKETING_DIDI_V5_COLLIDING_DATA,
        selectorExpression = MarketingOutsideInterfaceConstants.TAG_MARKETING_DIDI_V5_COLLIDING_DATA,
        consumeThreadNumber = 5, consumeThreadMax = 20, awaitTerminationMillisWhenShutdown = 10000)
public class DiDiV5CollidingDataConsumer extends BaseMqMessageListener
        implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    private RocketMqConsumerService consumerService;

    @Resource
    private DiDiCollidingDataService diDiCollidingDataService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.warn("MARKETING_DIDI_V5_COLLIDING_DATA：" +
                        "storeTimestamp[{}]msgId[{}]brokerName[{}]topic[{}]tags[{}]获取消息成功:{}"
                , messageExt.getStoreTimestamp(), messageExt.getMsgId()
                , messageExt.getBrokerName(), messageExt.getTopic()
                , messageExt.getTags(), bodyString);
        consumerService.consumerRun(messageExt, diDiCollidingDataService::saveDiDiCollidingDataLog, bodyString);
    }

    @Override
    protected void overMaxRetryTimesMessage(MessageExt messageExt) {

    }

    @Override
    protected boolean isThrowException() {
        return true;
    }

    @Override
    public void onMessage(MessageExt messageExt) {
        super.dispatchMessage(messageExt);
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer defaultMQPushConsumer) {

    }
}
