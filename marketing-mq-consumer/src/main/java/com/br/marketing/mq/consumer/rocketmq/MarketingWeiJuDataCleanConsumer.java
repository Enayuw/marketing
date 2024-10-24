package com.br.marketing.mq.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingUploadConstants;
import com.br.marketing.dto.xiecheng.XieChengActivateDTO;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
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
 * 消费 微距上传数据清洗消费端
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-10-24
 */
@Slf4j
@Service
@RocketMQMessageListener(nameServer = "${rocketmq.name-server:}",
        topic = MarketingUploadConstants.TOPIC,
        consumerGroup = MarketingUploadConstants.MARKETING_XIECHENG_COLLIDING_ACTIVATE,
        selectorExpression = MarketingUploadConstants.TAG_MARKETING_XIECHENG_COLLIDING_ACTIVATE)
public class MarketingWeiJuDataCleanConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private XieChengRobDataCollidingService robDataCollidingService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        log.warn("MARKETING_XIECHENG_COLLIDING_ACTIVATE：获取消息成功:{}",bodyString);
        XieChengActivateDTO xieChengActivateDTO = JSON.parseObject(bodyString,
                new TypeReference<XieChengActivateDTO>() {
                }.getType());
        consumerService.consumerRun(messageExt, robDataCollidingService::activateDataHandle, xieChengActivateDTO, null);
    }

    @Override
    protected void overMaxRetryTimesMessage(MessageExt messageExt) {
        log.warn("overMaxRetryTimes messageExt is [{}]", JSON.toJSONString(messageExt));
    }

    @Override
    protected boolean isThrowException() {
        log.warn("messageExt ThrowException");
        // true会重新消费消息
        return true;
    }

    @Override
    public void onMessage(MessageExt messageExt) {
        super.dispatchMessage(messageExt);
    }
}
