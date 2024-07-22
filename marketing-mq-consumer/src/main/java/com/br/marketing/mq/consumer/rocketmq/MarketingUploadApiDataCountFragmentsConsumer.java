package com.br.marketing.mq.consumer.rocketmq;

import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.MarketingSyncReportService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 上传接口接收数据量级碎片队列消费端
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-07-18
 */
@Slf4j
@Service
@RocketMQMessageListener(endpoints = "${rocketmq.consumer.endpoints:}",
        topic = MQConstants.MARKETINGEXCHANGER_NAME,
        consumerGroup = MQConstants.MARKETING_UPLOAD_API_DATA_COUNT_FRAGMENTS,// TODO 必须验证下RoutingKey带*的数据
        tag = MQConstants.BINDING_KEY_MARKETING_UPLOAD_API_COLLECTION_FRAGMENTS,consumptionThreadCount = 20)
public class MarketingUploadApiDataCountFragmentsConsumer extends BaseMqMessageListener implements RocketMQListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private MarketingSyncReportService marketingSyncReportService;

    @Override
    protected ConsumeResult handleMessage(MessageView messageView) throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String bodyString = charset.decode(messageView.getBody()).toString();
        log.warn("MARKETING_UPLOAD_API_DATA_COUNT_FRAGMENTS：获取消息成功:{}",bodyString);
        consumerService.consumerRun(messageView, marketingSyncReportService::nearRealtimeDataCountFragmentsStatis, bodyString, null);
        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        return super.dispatchMessage(messageView);
    }
}
