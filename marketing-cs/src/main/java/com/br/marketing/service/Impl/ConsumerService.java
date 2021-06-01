package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Function;

@Service
public class ConsumerService {

    @Autowired
    private RabbitMqProducter producter;

    public <T>void consumerRun(Channel channel, Message message, Function<T,Result<Boolean>> method,T t,String retry_routeKey){
        Result<Boolean> apply = method.apply(t);
        try {
            /**
             * code 为SUCCESS 认为消费成功
             *      根据返回结果来判断是否需要重新推送队列 false-不需要；true需要
             * code 为False 任务消费失败，重推队列
             */
            if(ResultCode.SUCCESS.getValue().equals(apply.getCode())){
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                if(apply.getData()){
                    if(!StringUtils.isBlank(retry_routeKey)){
                      producter.send(retry_routeKey,new String(message.getBody()));
                    }else{
                      producter.send(message.getMessageProperties().getReceivedRoutingKey(),new String(message.getBody()));
                    }
                }
            }else{
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Result<Boolean> test(String s){
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(false);
    }
}
