package com.br.marketing.check.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.PushRuleService;
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
 *
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/20 20:57
 */
@Slf4j
@Service
@RocketMQMessageListener(endpoints = "${rocketmq.consumer.endpoints:}",
        topic = MarketingOutsideInterfaceConstants.TOPIC,
        consumerGroup = MarketingOutsideInterfaceConstants.MARKETING_PUSH_SEVEN_FILETRANSFER,
        tag = MarketingOutsideInterfaceConstants.TAG_MARKETING_PUSH_SEVEN_FILETRANSFER,consumptionThreadCount = 20)
public class MarketingPushSevenFileTransferConsumer extends BaseMqMessageListener implements RocketMQListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    PushDataService pushDataService;

    @Override
    protected ConsumeResult handleMessage(MessageView messageView) throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String bodyString = charset.decode(messageView.getBody()).toString();
        Long o = JSON.parseObject(bodyString, new TypeReference<Long>() {
        }.getType());
        log.warn("MARKETING_PRE_USER_RECEIVE：获取消息成功:{}",o);
        consumerService.consumerRun(messageView, pushDataService::pushSevenTransferData, o, "");
        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        return super.dispatchMessage(messageView);
    }
}
