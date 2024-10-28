package com.br.marketing.innerapi.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingDelayedConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.strategy.InterfaceHandlerService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * 上传、转化数据通用处理延迟消费队列
 * 代码调整时记得看看消费端 {@link MarketingUniversalTransferReceiveCustomer}
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-21
 */
@Slf4j
@Service
@RocketMQMessageListener(nameServer = "${rocketmq.name-server:}",
        topic = MarketingDelayedConstants.TOPIC,
        consumerGroup = MarketingDelayedConstants.MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR,
        selectorExpression = MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR+"||"
                +MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_ERROR_DELAY)
public class MarketingUniversalTransferReceiveDelayConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private InterfaceHandlerService interfaceHandlerService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        log.warn("Marketing_Universal_Transfer_Receive_Delay_HalfHour：获取消息成功:{}",bodyString);
        /*消费逻辑*/
        consumerService.consumerRun(messageExt, interfaceHandlerService::handleDataDirection, bodyString, MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_ERROR_DELAY);
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
