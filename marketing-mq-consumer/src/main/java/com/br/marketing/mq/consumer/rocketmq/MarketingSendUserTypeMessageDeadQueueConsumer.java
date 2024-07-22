package com.br.marketing.mq.consumer.rocketmq;

import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.VariableDicService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 发送场景消息死信队列
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-07-18
 */
@Slf4j
@Service
@RocketMQMessageListener(endpoints = "${rocketmq.consumer.endpoints:}",
        topic = MQConstants.MARKETINGEXCHANGER_DEAD_NAME,
        consumerGroup = MQConstants.MARKETING_SEND_USERTYPE_MESSAGE_DEAD_QUEUE,
        tag = MQConstants.ROUTING_KEY_MARKETING_SEND_USERTYPE_MESSAGE_DEAD_QUEUE,consumptionThreadCount = 20)
public class MarketingSendUserTypeMessageDeadQueueConsumer extends BaseMqMessageListener implements RocketMQListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    VariableDicService variableDicService;

    @Override
    protected ConsumeResult handleMessage(MessageView messageView) throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String bodyString = charset.decode(messageView.getBody()).toString();
        log.warn("MARKETING_SEND_USERTYPE_MESSAGE_DEAD_QUEUE：获取消息成功:{}",bodyString);
        consumerService.consumerRun(messageView, variableDicService::delaySendUserTypeMessage, bodyString, null);
        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        return super.dispatchMessage(messageView);
    }
}
