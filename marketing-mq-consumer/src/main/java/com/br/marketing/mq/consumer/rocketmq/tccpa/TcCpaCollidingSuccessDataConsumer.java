package com.br.marketing.mq.consumer.rocketmq.tccpa;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rocketmq.MarketingTcCpaConstants;
import com.br.marketing.dto.tccpa.TcyrCpaSuccessMqDTO;
import com.br.marketing.service.Impl.RocketMqConsumerService;
import com.br.marketing.service.tccpa.TcyrLoopCycleDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.rocketmq.rocketmq.listener.BaseMqMessageListener;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * 消费 同程CPA撞库成功数据消费
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = MarketingTcCpaConstants.TOPIC_MARKETING_TCYR_CPA_COLLIDING_SUCCESS_QUEUE,
        consumerGroup = MarketingTcCpaConstants.GROUP_MARKETING_TCYR_CPA_COLLIDING_SUCCESS_QUEUE,
        selectorExpression = MarketingTcCpaConstants.TAG_MARKETING_TCYR_CPA_COLLIDING_SUCCESS_QUEUE,
        consumeThreadMax = 20)
public class TcCpaCollidingSuccessDataConsumer extends BaseMqMessageListener implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {

    private final static String TITLE = "【同程易融CPA-colliding周期剔除任务】";

    @Autowired
    RocketMqConsumerService consumerService;

    @Resource
    private TcyrLoopCycleDataService tcyrLoopCycleDataService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    protected String consumerName() {
        return "";
    }

    @Override
    protected void handleMessage(MessageExt messageExt) throws Exception {
        try {
            long start = System.currentTimeMillis();
            Integer priority = marketingCommonConfig.getTcyrCpaCollidingDealShardConfig().getInteger("priority");
            String bodyString = new String(messageExt.getBody(), StandardCharsets.UTF_8);
            log.warn("TITLE:{}MQ消费,msgId:{},msgBody:{}",TITLE,messageExt.getMsgId(),bodyString);
            if (priority > 1) {
                TcyrCpaSuccessMqDTO tcCpaSuccessMqDTO = JSON.parseObject(bodyString,
                        new TypeReference<TcyrCpaSuccessMqDTO>() {}.getType());
                log.warn("TITLE:{}MQ消费,msgId:{}, requestId:{}, dataId:{}",
                        TITLE,messageExt.getMsgId(),tcCpaSuccessMqDTO.getRequestId(),tcCpaSuccessMqDTO.getDataId());
                if (priority > 3) {
                    Result<Boolean> result = tcyrLoopCycleDataService.process(tcCpaSuccessMqDTO);
                    log.warn("TITLE:{}-process执行完成 MQ消费耗时:{}ms,msgId:{},requestId:{}, dataId:{}, resultCode:{}, needRetry:{}",
                            TITLE,System.currentTimeMillis() - start,messageExt.getMsgId(),
                            tcCpaSuccessMqDTO.getRequestId(), tcCpaSuccessMqDTO.getDataId(),
                            result.getCode(), result.getData());
                    if (priority > 5) {
                        consumerService.consumerRun(messageExt, (TcyrCpaSuccessMqDTO ignored) -> result, tcCpaSuccessMqDTO);
                        log.warn("TITLE:{}-handleMessage执行完成 MQ消费耗时:{}ms,msgId:{},requestId:{}, dataId:{}",
                                TITLE,System.currentTimeMillis() - start,messageExt.getMsgId(),
                                tcCpaSuccessMqDTO.getRequestId(), tcCpaSuccessMqDTO.getDataId()
                        );
                    }
                }
            }
        }catch (Exception e) {
            log.warn("TITLE:{},handleMessage异常",TITLE, e);
            try {
                log.warn("TITLE:{},handleMessage异常,requestId:{},msgBody:{}",TITLE,messageExt.getMsgId(), new String(messageExt.getBody(), StandardCharsets.UTF_8), e);
            }catch (Exception e1) {
                log.warn("TITLE:{},handleMessage异常-line84日志异常",TITLE, e1);
            }
            throw e;
        }
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

    @Override
    public void prepareStart(DefaultMQPushConsumer defaultMQPushConsumer) {
        defaultMQPushConsumer.setClientRebalance(false);
        defaultMQPushConsumer.setPopInvisibleTime(300000L);
    }
}
