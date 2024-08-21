package com.br.marketing.innerapi.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
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
 * 消费 黑名单
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/19 11:33
 */
@Slf4j
@Service
@RocketMQMessageListener(endpoints = "${rocketmq.consumer.endpoints:}",
        topic = MarketingOutsideInterfaceConstants.TOPIC,
        consumerGroup = MarketingOutsideInterfaceConstants.MARKETING_TRANSFER_PUSH_BLACK,
        tag = MarketingOutsideInterfaceConstants.TAG_MARKETING_TRANSFER_PUSH_BLACK,consumptionThreadCount = 20)
public class MarketingTransferPushBlackCustomer extends BaseMqMessageListener implements RocketMQListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    PushRuleService pushRuleService;

    @Override
    protected ConsumeResult handleMessage(MessageView messageView) throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String bodyString = charset.decode(messageView.getBody()).toString();
        Long o = JSON.parseObject(bodyString, new TypeReference<Long>() {}.getType());
        log.warn("Marketing_Transfer_Push_Black：获取消息成功:{}",o);
        /*消费逻辑*/
        consumerService.consumerRun(messageView, pushRuleService::consumerBlack, o, null);
        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        return super.dispatchMessage(messageView);
    }
}
