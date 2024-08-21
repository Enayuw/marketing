package com.br.marketing.mq.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.PushDataService;
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
 * 消费 携程消费
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-07-18
 */
@Slf4j
@Service
@RocketMQMessageListener(endpoints = "${rocketmq.consumer.endpoints:}",
        topic = MarketingAssistConstants.TOPIC,
        consumerGroup = MarketingAssistConstants.MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE,
        tag = MarketingAssistConstants.TAG_MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE,consumptionThreadCount = 20)
public class MarketingUniversalSftpToDbXieChengReceiveConsumer extends BaseMqMessageListener implements RocketMQListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    PushDataService pushDataService;

    @Override
    protected ConsumeResult handleMessage(MessageView messageView) throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String bodyString = charset.decode(messageView.getBody()).toString();
        log.warn("MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE：获取消息成功:{}",bodyString);
        consumerService.consumerRun(messageView, pushDataService::pushXieChengToDbData, bodyString, null);
        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        return super.dispatchMessage(messageView);
    }
}
