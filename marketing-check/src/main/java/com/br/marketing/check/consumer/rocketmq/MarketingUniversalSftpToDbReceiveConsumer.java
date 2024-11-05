package com.br.marketing.check.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.config.RocketMQSwitch;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.PushDataService;
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
 *
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/20 20:57
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingAssistConstants.TOPIC,
        consumerGroup = MarketingAssistConstants.MARKETING_UNIVERSAL_SFTPTODB_RECEIVE,
        selectorExpression = MarketingAssistConstants.TAG_MARKETING_UNIVERSAL_SFTPTODB_RECEIVE,
        consumeThreadNumber = 1, consumeThreadMax = 1)
public class MarketingUniversalSftpToDbReceiveConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    PushDataService pushDataService;
    @Resource
    private RocketMQSwitch rocketMQSwitch;
    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        Long o = JSON.parseObject(bodyString, new TypeReference<Long>() {
        }.getType());
        if(rocketMQSwitch.rocketLogSwitchFlag(MarketingAssistConstants.TAG_MARKETING_UNIVERSAL_SFTPTODB_RECEIVE)){
            log.warn("Marketing_Universal_SftpToDb_Receive：storeTimestamp[{}]msgId[{}]brokerName[{}]topic[{}]tags[{}]获取消息成功:{}"
                    , messageExt.getStoreTimestamp(), messageExt.getMsgId()
                    , messageExt.getBrokerName(), messageExt.getTopic()
                    , messageExt.getTags(), o);
        }
        consumerService.consumerRun(messageExt, pushDataService::pushSftpToDbData, o);
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
