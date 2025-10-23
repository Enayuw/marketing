package com.br.marketing.xcconsumer.consumer.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.dto.xiecheng.XieChengReportMessageDTO;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.Impl.xc.XieChengReportService;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;


/**
 * -----------------------------
 * PackageName： com.br.marketing.xcconsumer.consumer.rocketmq
 * ClassName：AbstractXieChengReportConsumer
 * Description：
 *
 * @author：it-yml CreateTime：2025-07-24
 * -----------------------------
 */
@Slf4j
@Service
public abstract class AbstractXieChengReportConsumer extends BaseMqMessageListener
        implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    protected RocketMqConsumerService consumerService;

    @Autowired
    protected XieChengReportService xieChengReportService;

    @Override
    protected String consumerName() {
        return null;
    }

    @Override
    protected void handleMessage(MessageExt messageExt) {
        log.warn("消费端 - consumerName:{}",this.consumerName());
        String bodyString = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        XieChengReportMessageDTO messageDTO = JSON.parseObject(bodyString, new TypeReference<XieChengReportMessageDTO>() {}.getType());
        logMessage(messageExt, bodyString);
        consumerService.consumerRun(messageExt, xieChengReportService::pushXieChengData, messageDTO);
    }

    protected void logMessage(MessageExt messageExt, String messageStr) {
        log.warn("MARKETING_XIECHENG_REPORT_QUEUE：storeTimestamp[{}]msgId[{}]brokerName[{}]topic[{}]tags[{}]获取消息成功:{}",
                messageExt.getStoreTimestamp(), messageExt.getMsgId(),
                messageExt.getBrokerName(), messageExt.getTopic(),
                messageExt.getTags(), messageStr);
    }

    @Override
    protected void overMaxRetryTimesMessage(MessageExt messageExt) {
        // 可添加自定义处理逻辑
    }

    @Override
    protected boolean isThrowException() {
        return true;
    }

    @Override
    public void onMessage(MessageExt messageExt) {
        super.dispatchMessage(messageExt);
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer defaultMQPushConsumer) {
        defaultMQPushConsumer.setClientRebalance(false);
        defaultMQPushConsumer.setPopInvisibleTime(300000L);
    }
}
