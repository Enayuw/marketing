package com.br.marketing.push.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingDelayedConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.push.service.impl.MergeWithMessageServiceImpl;
import com.br.marketing.service.Impl.RocketMqConsumerService;
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
 * 代码调整时记得看看消费端 {@link MarketingOffLineTaskFileCallBackConsumer}
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-21
 */
@Slf4j
@Service
@RocketMQMessageListener(endpoints = "${rocketmq.consumer.endpoints:}",
        topic = MarketingDelayedConstants.TOPIC,
        consumerGroup = MarketingDelayedConstants.MARKETING_OFFLINETASK_FILE_CALLBACK_ERRORDELAY,
        tag = MarketingDelayedConstants.TAG_MARKETING_OFFLINETASK_FILE_CALLBACK_ERRORDELAY,consumptionThreadCount = 20)
public class MarketingOffLineTaskFileCallBackErrorDelayConsumer extends BaseMqMessageListener implements RocketMQListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    MergeWithMessageServiceImpl mergeWithMessageService;

    @Override
    protected ConsumeResult handleMessage(MessageView messageView) throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String bodyString = charset.decode(messageView.getBody()).toString();
        Long o = JSON.parseObject(bodyString, new TypeReference<Long>() {
        }.getType());
        log.warn("Marketing_PushTask_File_Merge_ErrorDelay：获取消息成功:{}",o);
        consumerService.consumerRun(messageView, mergeWithMessageService::consumerFileCallBack, o, MQConstants.ROUTING_KEY_OFFLINETASK_FILE_CALLBACK_ERRORDELAY);
        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        return super.dispatchMessage(messageView);
    }
}
