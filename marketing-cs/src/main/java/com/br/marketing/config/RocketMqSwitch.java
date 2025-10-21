package com.br.marketing.config;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.rocketmq.RocketMqSwitchEntity;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.SecureRandom;


/**
 * RocketMQ和RabbitMQ切换开关
 *
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-22
 */
@Slf4j
@Component
public class RocketMqSwitch {
    /**
     * speed中配置启用RocketMQ的apiCode
     * 多个以逗号分隔
     */
    public static final String APICODES_SPEED = "apiCodes";
    /**
     * TAG对应的开关
     */
    public static final String FLAG = "flag";
    /**
     * 消费端日志打印开关
     */
    public static final String PRINT_LOG = "printLog";

    /**
     * 2025/7/31 13:22
     * 流量权重
     */
    public static final String FEATURE_WEIGHT = "featureWeight";

    private final SecureRandom RANDOM = new SecureRandom();

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private RocketMqTemplate template;
    @Resource
    private RabbitMqProducter rabbitMqProducter;
    public Boolean rocketMQSwitchFlag(String apiCode, String tag) {
        try {
            RocketMqSwitchEntity entity = marketingCommonConfig.getRocketMqSwitch2();
            if (entity == null) {
                return Boolean.FALSE;
            }
            Boolean global = entity.getGlobal();
            if (global == null) {
                return Boolean.FALSE;
            }
            if (global) {
                boolean flagValue = getMsgFlag(tag, FLAG, Boolean.FALSE);
                if (flagValue) {
                    return shouldRouteToRocketMq(entity, tag);
                } else {
                    String appCodesValue = getGroupValue(entity, tag, APICODES_SPEED, null, String.class);
                    if (StringUtils.isBlank(apiCode) || StringUtils.isBlank(appCodesValue)) {
                        return Boolean.FALSE;
                    }
                    return appCodesValue.contains(apiCode) && shouldRouteToRocketMq(entity, tag);
                }
            }
        } catch (Exception e) {
            log.warn("rocketMQSwitchFlag对应的RocketMqSwitch2配置异常,apiCode:{}--tag:{}--", apiCode, tag, e);
        }
        return Boolean.FALSE;
    }

    /**
     * 2025/7/31 13:22
     * 获取流量权重
     */
    public boolean shouldRouteToRocketMq(RocketMqSwitchEntity entity, String tag) {
        Integer featureWeight = getGroupValue(entity, tag, FEATURE_WEIGHT, null, Integer.class);
        if (featureWeight == null || featureWeight <= 0) {
            if (entity.getFeatureWeight() == null || entity.getFeatureWeight() <= 0) {
                return true;
            } else {
                featureWeight = entity.getFeatureWeight();
            }
        }
        int weight = RANDOM.nextInt(100) + 1;
        return weight <= featureWeight;
    }

    public void sendMessage(String apiCode, String topic, String tag, String msg, String routeKey) {
        if (rocketMQSwitchFlag(apiCode, tag)) {
            syncSend(topic, tag, msg);
        } else {
            rabbitMqProducter.send(routeKey, msg);
        }
    }

    public boolean rocketLogSwitchFlag(String tag) {
        return getMsgFlag(tag, PRINT_LOG, false);
    }

    public <T> void rocketLogSwitchFlag(String tag, MessageExt messageExt, T t, long startTimeMillis) {
        if (rocketLogSwitchFlag(tag)) {
            log.warn("rocketLogSwitchFlag--耗时[{}ms]--tag[{}];message[{}];messageExt[{}]"
                    , (System.currentTimeMillis() - startTimeMillis), tag, t, messageExt);
        }
    }

    public <T> SendResult syncSend(String topic, String tag, T msg) {
        Message<?> build = MessageBuilder.withPayload(msg).build();
        return template.syncSendMessage(topic, tag, build);
    }

    public <T> SendResult syncSendDelaySecond(String topic, String tag, T msg, long delayTime) {
        Message<?> build = MessageBuilder.withPayload(msg).build();

        return template.syncSendDelaySecond(topic, tag, build, delayTime);
    }



    /**
     * 2025/6/11 01:54
     * 获取boolean类型的开关
     */
    private boolean getMsgFlag(String tag, String key, boolean localFlag) {
        try {
            RocketMqSwitchEntity entity = marketingCommonConfig.getRocketMqSwitch2();
            if (entity == null) {
                return localFlag;
            }
            return getGroupValue(entity, tag, key, localFlag, Boolean.class);
        } catch (Exception e) {
            log.warn("{},tag:{},key:{},localFlag:{}", e.getMessage(), tag, key, localFlag, e);
        }
        return localFlag;
    }


    private <T> T getGroupValue(RocketMqSwitchEntity entity, String tag, String key, T localVale, Class<T> tClass) {
        if (entity == null) {
            return localVale;
        }
        JSONObject group = entity.getGroup();
        if (group == null || group.isEmpty()) {
            return localVale;
        }
        JSONObject tagObjet = group.getJSONObject(tag);
        if (tagObjet == null || tagObjet.isEmpty()) {
            return localVale;
        }
        T value = tagObjet.getObject(key, tClass);
        return value == null ? localVale : value;
    }

}
