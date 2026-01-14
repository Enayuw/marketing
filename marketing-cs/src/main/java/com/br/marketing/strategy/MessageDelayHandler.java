package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.constants.rocketmq.MarketingDelayedConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    //消息过期时间 1h
    private static final String EXPIRE_TIME = "3600000";

    @Resource
    private RabbitMqProducter producer;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;
    @Resource
    private SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;

    @Override
    JSONObject call(List<MqFact> mqFacts, ProcessHandlerContext context) {
        String expireTime = StringUtils.hasText(marketingCommonConfig.getMessageQueueExpireTime())
                ?marketingCommonConfig.getMessageQueueExpireTime():EXPIRE_TIME;

        for (MqFact mqFact : mqFacts) {
            mqFact.setIdempotentKey(snowflakeRedisGeneratorHandle.nextId());
            String message = JSON.toJSONString(mqFact);
            if (!StringUtils.isEmpty(mqFact.getDelayTime()) && mqFact.getDelayTime() > 0) {
                float v = mqFact.getDelayTime() * Integer.parseInt(expireTime);
                if(rocketMqSwitch.rocketMQSwitchFlag(null, MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR)){
                    rocketMqSwitch.syncSendDelaySecond(MarketingDelayedConstants.TOPIC
                            , MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR, message
                            , (int)v/1000);
                }else{
                    producer.sendByExpiration(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALF_HOUR, message, String.valueOf((int)v));
                }
            }else{
                if(rocketMqSwitch.rocketMQSwitchFlag(null, MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR)){
                    rocketMqSwitch.syncSendDelaySecond(MarketingDelayedConstants.TOPIC
                            , MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR, message
                            , Integer.parseInt(expireTime)/1000);
                }else{
                    producer.sendByExpiration(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE_DELAY,message,expireTime);
                }
            }
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.MESSAGE_DELAY;
    }
}
