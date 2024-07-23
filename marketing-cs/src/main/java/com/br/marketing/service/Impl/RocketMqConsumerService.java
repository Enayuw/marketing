package com.br.marketing.service.Impl;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
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

    public static Boolean consumerDownStatus = Boolean.FALSE;

    /**
     * rabbitMQ消费端
     * @param messageView 消费消息
     * @param method 消费业务
     * @param t 消费信息
     * @param retryRouteKey 重试路由key
     * @param <T> 消费消息类型
     */
    public <T> Boolean consumerRun(MessageView messageView, Function<T, Result<Boolean>> method, T t, String retryRouteKey) {
        String message = null;
        try {
            /**
             * 下线标识，不在消费消息
             */
            if(consumerDownStatus){
                log.warn("服务下线，消费者不在接收新的流量");
                Thread.sleep(10000L);
                log.warn("服务下线，消费者休眠时间到");
            }
            Charset charset = StandardCharsets.UTF_8;
            message = charset.decode(messageView.getBody()).toString();
            Result<Boolean> apply = method.apply(t);
            /**
             * code 为SUCCESS 认为消费成功
             *      根据返回结果来判断是否需要重新推送队列 false-不需要；true需要
             * code 为False 任务消费失败，重推队列
             */
            if (ResultCode.SUCCESS.getValue().equals(apply.getCode())) {
//                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                if (apply.getData()) {
                    if (StringUtils.isNotBlank(retryRouteKey)) {
                        template.syncSend(messageView.getTopic(), retryRouteKey, message);
//                        producter.send(retryRouteKey, new String(message.getBody(), StandardCharsets.UTF_8));
                    } else {
                        template.syncSend(messageView.getTopic(), messageView.getTag().get(), message);
//                        producter.send(message.getMessageProperties().getReceivedRoutingKey(), new String(message.getBody(), StandardCharsets.UTF_8));
                    }
                }
                return Boolean.TRUE;
            } else {
//                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        } catch (Exception e) {
            template.syncSend(messageView.getTopic(), messageView.getTag().get(), message);
            String error = String.format("路由键：%s,\r\n消息内容：%s,\r\n错误信息：%s"
                    , messageView.getTag()
                    , message
                    , e.getMessage());
            log.error(error,e);
            alarmClient.sendAlarm(error,"消费异常", AlarmSendCodeEnum.ERROR_UNKNOWN.getCode());
//            try {
//                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
//            } catch (IOException ioException) {
//                ioException.printStackTrace();
//            }
        }
        return Boolean.FALSE;
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
