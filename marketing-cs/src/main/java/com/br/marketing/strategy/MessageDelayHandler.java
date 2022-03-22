package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 消息延迟处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/14 10:56
 */

@Service
public class MessageDelayHandler extends AbstractExternalInterfaceHandler<MqFact>{

    //消息过期时间 1h
    private static final String EXPIRE_TIME = "600000";

    @Resource
    private RabbitMqProducter producer;

    @Override
    JSONObject call(List<MqFact> mqFacts, ProcessHandlerContext context) {

        //todo 将需要静置的数据，重新放置到延迟队列里

        for (MqFact mqFact : mqFacts) {
            String message = JSON.toJSONString(mqFact);
            producer.sendByExpiration(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE_DELAY,message,EXPIRE_TIME);
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.MESSAGE_DELAY;
    }
}
