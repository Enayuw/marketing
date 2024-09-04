package com.br.marketing.innerapi.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingDelayedConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.strategy.InterfaceHandlerService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 上传、转化数据通用处理延迟消费队列
 * 代码调整时记得看看消费端 {@link MarketingUniversalTransferReceiveCustomer}
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-21
 */
@Slf4j
@Service
@RocketMQMessageListener(endpoints = "${rocketmq.consumer.endpoints:}",
        topic = MarketingDelayedConstants.TOPIC,
        consumerGroup = MarketingDelayedConstants.MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE,
        tag = MarketingDelayedConstants.TAG_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE+"||"
                +MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_ERROR_DELAY,
        consumptionThreadCount = 20)
public class MarketingUniversalTransferReceiveDelayConsumer extends BaseMqMessageListener implements RocketMQListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private InterfaceHandlerService interfaceHandlerService;

    @Override
    protected ConsumeResult handleMessage(MessageView messageView) throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String bodyString = charset.decode(messageView.getBody()).toString();
        log.warn("Marketing_Send_UserType_Message_Delay_Queue：获取消息成功:{}",bodyString);
        /*消费逻辑*/
        consumerService.consumerRun(messageView, interfaceHandlerService::handleDataDirection, bodyString, MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_ERROR_DELAY);
        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        return super.dispatchMessage(messageView);
    }
}
