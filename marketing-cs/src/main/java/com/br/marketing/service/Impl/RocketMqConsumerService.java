package com.br.marketing.service.Impl;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rocketmq.MarketingDelayedConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * 替换原来的ConsumerService
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-07-18
 */
@Slf4j
@Service
public class RocketMqConsumerService {

    @Resource
    private AlarmApiClient alarmClient;

    @Autowired
    private RocketMqTemplate template;
    /**
     * RocketMQ消费端
     * @param messageExt 消费消息
     * @param method 消费业务
     * @param t 消费信息
     * @param retryTag Tag（消息重试使用）
     *                 使用时去marketing-utils/src/main/java/com/br/marketing/common/constants/rocketmq 包中核对
     * @param delayTopic 消息延时对应的延时队列
     *                   使用时去marketing-utils/src/main/java/com/br/marketing/common/constants/rocketmq 包中核对
     * @param delayTime 消息延时时间（单位：秒） delayTime
     *                  delayTime>0时，发送到延时Topic下
     */
    public <T> void consumerRun(MessageExt messageExt, Function<T, Result<Boolean>> method, T t
            , String delayTopic, String retryTag, long delayTime) {
        String message = null;
        try {
            Result<Boolean> apply = method.apply(t);
            /**
             * code 为SUCCESS 认为消费成功
             *      根据返回结果来判断是否需要重新推送队列 false-不需要；true需要
             * code 为False 任务消费失败，重推队列
             */
            if (ResultCode.SUCCESS.getValue().equals(apply.getCode())) {
                if (apply.getData()) {
                    message = new String(messageExt.getBody(),StandardCharsets.UTF_8);
                    if (StringUtils.isNotBlank(delayTopic) && StringUtils.isNotBlank(retryTag)) {
                        if(delayTime>0){
                            // 根据消费端配置的[延迟Topic]和[Tags]发送
                            template.syncSendDelaySecond(delayTopic, retryTag, message, delayTime);
                        }else{
                            // 根据消费端配置的[普通Topic]和[Tags]发送
                            template.syncSend(delayTopic, retryTag, message);
                        }
                    } else {
                        // 消息重新入本队列
                        template.syncSend(messageExt.getTopic(), messageExt.getTags(), message);
                    }
                }
            } else {
                /**
                 * 消息重试，默认消息重试规则：
                 * 第几次重试	与上次重试的间隔时间	第几次重试	与上次重试的间隔时间
                 * 1	    10秒	            9	        7分钟
                 * 2	    30秒	            10	        8分钟
                 * 3	    1分钟	            11	        9分钟
                 * 4	    2分钟	            12	        10分钟
                 * 5	    3分钟	            13	        20分钟
                 * 6	    4分钟	            14	        30分钟
                 * 7	    5分钟	            15	        1小时
                 * 8	    6分钟	            16	        2小时
                 */
                throw new RuntimeException();
            }
        } catch (Exception e) {
            String error = String.format("RocketMQ消费异常topic：%s,Tags：%s,msgId：%s,message：%s,\r\n错误信息：%s"
                    , messageExt.getTopic()
                    , messageExt.getTags()
                    , messageExt.getMsgId()
                    , message
                    , e.getMessage());
            log.warn(error,e);
            alarmClient.sendAlarm(error,"RocketMQ消费异常", AlarmSendCodeEnum.ROCKETMQ_CONSUMER_ERROR.getCode());
            throw new RuntimeException();
        }
    }

    /**
     * RocketMQ消费端
     * @param messageExt 消费消息
     * @param method 消费业务
     * @param t 消费信息
     * @param <T> 消费消息类型
     */
    public <T> void consumerRun(MessageExt messageExt, Function<T, Result<Boolean>> method, T t) {
        consumerRun(messageExt, method, t, null, null, 0L);
    }

    /**
     * pulsar消费端
     * @param subscription 订阅者
     * @param method 消费业务方法
     * @param consumerNum 消费者数量
     * @param topic 主题，死信，重试
     */
    public void consumerPulsar(String subscription,Function<String, Result<Boolean>> method,Integer consumerNum,String... topic) {
        if(consumerNum == null || consumerNum<=0){
            consumerNum = 1;
        }
        for (int i=0;i<consumerNum;i++){
            new PulsarConsumerThread(method,subscription,topic).start();
        }
    }

}
