package com.br.marketing.mq.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.zhongyou.ZhongYouDataService;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.service.Impl.RocketMqConsumerService;
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
 * 消费 中邮清洗数据
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-07-18
 */
@Slf4j
@Service
@RocketMQMessageListener(endpoints = "${rocketmq.consumer.endpoints:}",
        topic = MarketingAssistConstants.TOPIC,
        consumerGroup = MarketingAssistConstants.MARKETING_ZHONGYOU_DATA_CLEAN,
        tag = MarketingAssistConstants.TAG_MARKETING_ZHONGYOU_DATA_CLEAN,consumptionThreadCount = 20)
public class MarketingZhongYouDataCleanConsumer extends BaseMqMessageListener implements RocketMQListener {

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private ZhongYouDataService zhongYouDataService;

    @Override
    protected ConsumeResult handleMessage(MessageView messageView) throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String bodyString = charset.decode(messageView.getBody()).toString();
        Long o = JSON.parseObject(bodyString, new TypeReference<Long>() {
        }.getType());
        log.warn("MARKETING_ZHONGYOU_DATA_CLEAN：获取消息成功:{}",o);
        consumerService.consumerRun(messageView, zhongYouDataService::HandleZhongYouData, o, null);
        return ConsumeResult.SUCCESS;
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        return super.dispatchMessage(messageView);
    }
}
