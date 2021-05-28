package com.br.marketing.api.rabbitmq;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.function.Function;


public class Consumer {

    public <T>void consumer(Channel channel, Message message, Function<T,Result> method,T t){
        Result apply = method.apply(t);
//        if(ResultCode.SUCCESS.getValue().equals(apply.getCode())){
//            channel.basicAck();
//        }
    };
}
