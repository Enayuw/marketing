package com.br.marketing.service.Impl;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Service
public class ConsumerService {

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);
    @Autowired
    private RabbitMqProducter producter;

    /**
     * rabbitMQ消费端
     * @param channel 渠道
     * @param message 消费消息
     * @param method 消费业务
     * @param t 消费信息
     * @param retryRouteKey 重试路由key
     * @param <T> 消费消息类型
     */
    public <T> void consumerRun(Channel channel, Message message, Function<T, Result<Boolean>> method, T t, String retryRouteKey) {
        try {
            Result<Boolean> apply = method.apply(t);
            /**
             * code 为SUCCESS 认为消费成功
             *      根据返回结果来判断是否需要重新推送队列 false-不需要；true需要
             * code 为False 任务消费失败，重推队列
             */
            if (ResultCode.SUCCESS.getValue().equals(apply.getCode())) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                if (apply.getData()) {
                    if (StringUtils.isNotBlank(retryRouteKey)) {
                        producter.send(retryRouteKey, new String(message.getBody(), StandardCharsets.UTF_8));
                    } else {
                        producter.send(message.getMessageProperties().getReceivedRoutingKey(), new String(message.getBody(), StandardCharsets.UTF_8));
                    }
                }
            } else {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        } catch (Exception e) {
            String error = String.format("路由键：%s,\r\n消息内容：%s,\r\n错误信息：%s"
                    , message.getMessageProperties().getReceivedRoutingKey()
                    , new String(message.getBody(), StandardCharsets.UTF_8)
                    , e.getMessage());
            log.error(error,e);
            alarmClient.sendAlarm(error,"消费异常", AlarmSendCodeEnum.ERROR_UNKNOWN.getCode());
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
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

    public Result<Boolean> test(String s) {
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }


}
