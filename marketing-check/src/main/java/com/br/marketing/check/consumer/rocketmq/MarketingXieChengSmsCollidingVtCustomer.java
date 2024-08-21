package com.br.marketing.check.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.XieChengSmsPushToTransferService;
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
        topic = MQConstants.MARKETINGEXCHANGER_NAME,
        consumerGroup = MQConstants.MARKETING_XIECHENG_SMSCOLLIDINGVT_CUSTOMER,
        tag = MQConstants.ROUTING_KEY_XIECHENG_SMSCOLLIDINGVT_CUSTOMER,consumptionThreadCount = 20)
public class MarketingXieChengSmsCollidingVtCustomer extends BaseMqMessageListener implements RocketMQListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    XieChengSmsPushToTransferService xieChengSmsPushToTransferService;

    @Override
    protected ConsumeResult handleMessage(MessageView messageView) throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String bodyString = charset.decode(messageView.getBody()).toString();
        log.warn("Marketing_XieChengSmsCollidingVt_Customer：获取消息成功:{}",bodyString);
        consumerService.consumerRun(messageView, xieChengSmsPushToTransferService::consumerXiechengSmsCollidingVtUser, bodyString, null);
        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        return super.dispatchMessage(messageView);
    }
}
