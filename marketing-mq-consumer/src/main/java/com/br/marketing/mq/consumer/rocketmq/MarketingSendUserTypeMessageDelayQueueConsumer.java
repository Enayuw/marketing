package com.br.marketing.mq.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingDelayedConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.VariableDicService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 发送场景消息延时队列
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-07-18
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingDelayedConstants.TOPIC,
        consumerGroup = MarketingDelayedConstants.MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE,
        selectorExpression = MarketingDelayedConstants.TAG_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE,
        consumeThreadNumber = 1, consumeThreadMax = 1)
public class MarketingSendUserTypeMessageDelayQueueConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    VariableDicService variableDicService;
    @Resource
    private RocketMqSwitch rocketMQSwitch;
    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        if(rocketMQSwitch.rocketLogSwitchFlag(MarketingDelayedConstants.TAG_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE)){
            log.warn("MARKETING_SEND_USERTYPE_MESSAGE_DEAD_QUEUE：" +
                            "storeTimestamp[{}]msgId[{}]brokerName[{}]topic[{}]tags[{}]获取消息成功:{}"
                    , messageExt.getStoreTimestamp(), messageExt.getMsgId()
                    , messageExt.getBrokerName(), messageExt.getTopic()
                    , messageExt.getTags(), bodyString);
        }
        consumerService.consumerRun(messageExt, variableDicService::delaySendUserTypeMessage, bodyString);
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

}
