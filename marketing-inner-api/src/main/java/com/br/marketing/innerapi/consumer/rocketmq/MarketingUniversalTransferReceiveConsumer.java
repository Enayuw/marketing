package com.br.marketing.innerapi.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingDelayedConstants;
import com.br.marketing.common.constants.rocketmq.MarketingTransferConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.config.RocketMqSwitch;
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
 * 上传、转化数据通用处理流程
 * 代码调整时记得看看延迟处理消费端 {@link MarketingUniversalTransferReceiveDelayConsumer}
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/19 11:33
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingTransferConstants.TOPIC,
        consumerGroup = MarketingTransferConstants.MARKETING_UNIVERSAL_TRANSFER_RECEIVE,
        selectorExpression = MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE,
        consumeThreadNumber = 2, consumeThreadMax = 5)
public class MarketingUniversalTransferReceiveConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private InterfaceHandlerService interfaceHandlerService;
    @Resource
    private RocketMqSwitch rocketMQSwitch;
    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        if(rocketMQSwitch.rocketLogSwitchFlag(MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE)){
            log.warn("Marketing_Universal_Transfer_Receive：storeTimestamp[{}]msgId[{}]brokerName[{}]topic[{}]tags[{}]获取消息成功:{}"
                    , messageExt.getStoreTimestamp(), messageExt.getMsgId()
                    , messageExt.getBrokerName(), messageExt.getTopic()
                    , messageExt.getTags(), bodyString);
        }
        consumerService.consumerRun(messageExt, interfaceHandlerService::handleDataDirection, bodyString
                , MarketingDelayedConstants.TOPIC
                , MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_ERROR_DELAY
                , 300L);
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
