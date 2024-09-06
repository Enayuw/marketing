package com.br.marketing.mq.consumer.rocketmq;

import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.MarketingSyncReportService;
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
 * 上传接口接收数据量级碎片队列消费端
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-07-18
 */
@Slf4j
@Service
@RocketMQMessageListener(nameServer = "${rocketmq.name-server:}",
        topic = MarketingAssistConstants.TOPIC,
        consumerGroup = MarketingAssistConstants.MARKETING_UPLOAD_API_DATA_COUNT_FRAGMENTS,// TODO 必须验证下RoutingKey带*的数据
        selectorExpression = MarketingAssistConstants.TAG_MARKETING_UPLOAD_API_DATA_COUNT_FRAGMENTS)
public class MarketingUploadApiDataCountFragmentsConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt> {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private MarketingSyncReportService marketingSyncReportService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        String bodyString = new String(messageExt.getBody(),StandardCharsets.UTF_8);
        log.warn("MARKETING_UPLOAD_API_DATA_COUNT_FRAGMENTS：获取消息成功:{}",bodyString);
        consumerService.consumerRun(messageExt, marketingSyncReportService::nearRealtimeDataCountFragmentsStatis, bodyString, null);
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

//    @Override
//    protected ConsumeResult handleMessage(MessageView messageView) throws Exception {
//        Charset charset = StandardCharsets.UTF_8;
//        String bodyString = charset.decode(messageView.getBody()).toString();
//        log.warn("MARKETING_UPLOAD_API_DATA_COUNT_FRAGMENTS：获取消息成功:{}",bodyString);
//        consumerService.consumerRun(messageView, marketingSyncReportService::nearRealtimeDataCountFragmentsStatis, bodyString, null);
//        return ConsumeResult.SUCCESS;
//    }
//
//    @Override
//    public ConsumeResult consume(MessageView messageView) {
//        return super.dispatchMessage(messageView);
//    }
}
