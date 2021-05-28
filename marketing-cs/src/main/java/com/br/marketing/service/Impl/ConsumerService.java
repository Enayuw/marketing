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
        if(ResultCode.SUCCESS.getValue().equals(apply.getCode())){
            if(!apply.getData()) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }else{
                if(!StringUtils.isBlank(retry_routeKey)){
                  producter.send(retry_routeKey,message.getBody());
                }else{
                  producter.send(message.getMessageProperties().getReceivedRoutingKey(),message);
                }
            }
        }else{
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Result<Boolean> test(String s){
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(false);
    }
}
