package com.br.marketing.push.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
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
 * 代码调整时记得看看消费端 {@link MarketingOffLineTaskFileCallBackErrorDelayConsumer}
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/20 20:57
 */
@Slf4j
@Service
@RocketMQMessageListener(nameServer = "${rocketmq.name-server:}",
        topic = MarketingAssistConstants.TOPIC,
        consumerGroup = MarketingAssistConstants.MARKETING_OFFLINETASK_FILE_CALLBACK,
        selectorExpression = MarketingAssistConstants.TAG_MARKETING_OFFLINETASK_FILE_CALLBACK)
public class MarketingOffLineTaskFileCallBackConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> {

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
        log.warn("Marketing_OffLineTask_File_CallBack：获取消息成功:{}",o);
        consumerService.consumerRun(messageExt, mergeWithMessageService::consumerFileCallBack, o
                , MQConstants.ROUTING_KEY_OFFLINETASK_FILE_CALLBACK_ERRORDELAY);
    }

    @Override
    protected void overMaxRetryTimesMessage(MessageExt messageExt) {

    }

    @Override
    protected boolean isThrowException() {
        return false;
    }

    @Override
    public void onMessage(MessageExt messageExt) {
        super.dispatchMessage(messageExt);
    }

}
