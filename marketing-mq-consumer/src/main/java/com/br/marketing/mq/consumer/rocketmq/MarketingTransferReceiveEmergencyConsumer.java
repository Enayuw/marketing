package com.br.marketing.mq.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingTransferEmergencyConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.PushRuleService;
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
 * 消费 原始转化数据消费端（大队列）
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-07-18
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingTransferEmergencyConstants.TOPIC,
        consumerGroup = MarketingTransferEmergencyConstants.MARKETING_TRANSFER_RECEIVE_EMERGENCY,
        selectorExpression = MarketingTransferEmergencyConstants.TAG_MARKETING_TRANSFER_RECEIVE_EMERGENCY,
        consumeThreadNumber = 1, consumeThreadMax = 1)
public class MarketingTransferReceiveEmergencyConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    PushRuleService pushRuleService;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        Long o = JSON.parseObject(bodyString, new TypeReference<Long>() {
        }.getType());
        if(rocketMqSwitch.rocketLogSwitchFlag(MarketingTransferEmergencyConstants.TAG_MARKETING_TRANSFER_RECEIVE_EMERGENCY)){
            log.warn("MARKETING_TRANSFER_RECEIVE_EMERGENCY：storeTimestamp[{}]msgId[{}]brokerName[{}]topic[{}]tags[{}]获取消息成功:{}"
                    , messageExt.getStoreTimestamp(), messageExt.getMsgId()
                    , messageExt.getBrokerName(), messageExt.getTopic()
                    , messageExt.getTags(), o);
        }
        consumerService.consumerRun(messageExt, pushRuleService::consumerTransferData, o);
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
