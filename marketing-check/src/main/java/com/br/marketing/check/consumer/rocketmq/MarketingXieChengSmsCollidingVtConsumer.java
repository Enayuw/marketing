package com.br.marketing.check.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.XieChengSmsPushToTransferService;
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
@RocketMQMessageListener(topic = MarketingOutsideInterfaceConstants.TOPIC,
        consumerGroup = MarketingOutsideInterfaceConstants.MARKETING_XIECHENGSMSCOLLIDINGVT_CUSTOMER,
        selectorExpression = MarketingOutsideInterfaceConstants.TAG_MARKETING_XIECHENGSMSCOLLIDINGVT_CUSTOMER,
        consumeThreadNumber = 2, consumeThreadMax = 5)
public class MarketingXieChengSmsCollidingVtConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    XieChengSmsPushToTransferService xieChengSmsPushToTransferService;
    @Resource
    private RocketMqSwitch rocketMQSwitch;
    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        if(rocketMQSwitch.rocketLogSwitchFlag(MarketingOutsideInterfaceConstants.TAG_MARKETING_XIECHENGSMSCOLLIDINGVT_CUSTOMER)){
            log.warn("Marketing_XieChengSmsCollidingVt_Customer：" +
                            "storeTimestamp[{}]msgId[{}]brokerName[{}]topic[{}]tags[{}]获取消息成功:{}"
                    , messageExt.getStoreTimestamp(), messageExt.getMsgId()
                    , messageExt.getBrokerName(), messageExt.getTopic()
                    , messageExt.getTags(), bodyString);
        }
        consumerService.consumerRun(messageExt, xieChengSmsPushToTransferService::consumerXiechengSmsCollidingVtUser, bodyString);
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
