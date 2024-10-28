package com.br.marketing.push.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingDelayedConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.push.service.impl.MergeWithMessageServiceImpl;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 *
 * 代码调整时记得看看消费端 {@link MarketingOffLineTaskFileCallBackConsumer}
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-21
 */
@Slf4j
@Service
@RocketMQMessageListener(nameServer = "${rocketmq.name-server:}",
        topic = MarketingDelayedConstants.TOPIC,
        consumerGroup = MarketingDelayedConstants.MARKETING_OFFLINETASK_FILE_CALLBACK_ERRORDELAY,
        selectorExpression = MarketingDelayedConstants.TAG_MARKETING_OFFLINETASK_FILE_CALLBACK_ERRORDELAY)
public class MarketingOffLineTaskFileCallBackErrorDelayConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMqConsumerService consumerService;

    @Autowired
    MergeWithMessageServiceImpl mergeWithMessageService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        Long o = JSON.parseObject(bodyString, new TypeReference<Long>() {
        }.getType());
        log.warn("Marketing_OffLineTask_File_CallBack_ErrorDelay：获取消息成功:{}",o);
        consumerService.consumerRun(messageExt, mergeWithMessageService::consumerFileCallBack, o, MQConstants.ROUTING_KEY_OFFLINETASK_FILE_CALLBACK_ERRORDELAY);
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
