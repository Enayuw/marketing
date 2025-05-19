package com.br.marketing.xcconsumer.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.Impl.xc.XieChengReportService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingXieChengConstants.TOPIC,
        consumerGroup = MarketingXieChengConstants.MARKETING_XIECHENG_REPORT,
        selectorExpression = MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT,
        consumeThreadNumber = 1, consumeThreadMax = 1)
public class MarketingXiechengReportQueueConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Resource
    XieChengReportService xieChengReportService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) {
        String bodyString = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        Long o = JSON.parseObject(bodyString, new TypeReference<Long>() {}.getType());
        if(rocketMqSwitch.rocketLogSwitchFlag(MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT)){
            log.warn("MARKETING_XIECHENG_REPORT_QUEUE" +
                            "：storeTimestamp[{}]msgId[{}]brokerName[{}]topic[{}]tags[{}]获取消息成功:{}"
                    , messageExt.getStoreTimestamp(), messageExt.getMsgId()
                    , messageExt.getBrokerName(), messageExt.getTopic()
                    , messageExt.getTags(), o);
        }
        consumerService.consumerRun(messageExt, xieChengReportService::pushXieChengData, o);
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
