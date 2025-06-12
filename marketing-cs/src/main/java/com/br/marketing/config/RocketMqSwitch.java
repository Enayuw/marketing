package com.br.marketing.config;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.rocketmq.RocketMqSwitchEntity;
import com.br.marketing.handle.MessageIdempotentHandler;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


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
     * 消息生成uuid
     */
    public static final String MSG_UUID_FLAG = "msgUUIdFlag";
    /**
     * 消息幂等
     */
    public static final String MSG_IDEM_FLAG = "msgIdemFlag";

    /**
     * 允许重复消费的秒数
     */
    public static final String ALLOW_REPROCESS_SECONDS = "allowReprocessSeconds";

    /**
     * mq消息头 生产消息的唯一标识
     */
    public static final String KEYS = "KEYS";

    @Resource
    private MessageIdempotentHandler messageIdempotentHandler;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private RocketMqTemplate template;
    @Resource
    private RabbitMqProducter rabbitMqProducter;

    @Value("${spring.application.name}")
    private String appName;

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
                    return Boolean.TRUE;
                } else {
                    String appCodesValue = getMsgFlagValue(tag, APICODES_SPEED, null, String.class);
                    if (StringUtils.isBlank(apiCode) || StringUtils.isBlank(appCodesValue)) {
                        return Boolean.FALSE;
                    }
                    return appCodesValue.contains(apiCode);
                }
            }
        } catch (Exception e) {
            log.warn("rocketMQSwitchFlag对应的RocketMqSwitch2配置异常,apiCode:{}--tag:{}--", apiCode, tag, e);
        }
        return Boolean.FALSE;
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
        Message<?> build;
        if (msgUUIdFlag(tag)) {
            build = MessageBuilder.withPayload(msg)
                    .setHeader(KEYS, messageIdempotentHandler.generateMessageId())
                    .build();
        } else {
            build = MessageBuilder.withPayload(msg).build();
        }
        return template.syncSendMessage(topic, tag, build);
    }

    public <T> SendResult syncSendDelaySecond(String topic, String tag, T msg, long delayTime) {
        Message<?> build;
        if (msgUUIdFlag(tag)) {
            build = MessageBuilder.withPayload(msg)
                    .setHeader(KEYS, messageIdempotentHandler.generateMessageId())
                    .build();
        } else {
            build = MessageBuilder.withPayload(msg).build();
        }
        return template.syncSendDelaySecond(topic, tag, build, delayTime);
    }


    /**
     * 2025/6/11 00:31
     * 开启uuid，如果配置 MSG_IDEM_FLAG为 true 则 强制开启 uuid
     */
    public boolean msgUUIdFlag(String tag) {
        return msgIdemFlag(tag) || getMsgFlag(tag, MSG_UUID_FLAG, true);
    }

    /**
     * 2025/6/11 00:31
     * 开启消息幂等
     */
    public boolean msgIdemFlag(String tag) {
        return getMsgFlag(tag, MSG_IDEM_FLAG, true);
    }

    public boolean msgIdemFlag(String tag, boolean localFlag) {
        return getMsgFlag(tag, MSG_IDEM_FLAG, localFlag);
    }

    public int getAllowReprocessSeconds(String tag, int localValue) {
        return getMsgFlagValue(tag, ALLOW_REPROCESS_SECONDS, localValue, Integer.class);
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
            JSONObject appNameFlag = entity.getAppNameFlag();
            if (null == appNameFlag) {
                return getGroupValue(entity, tag, key, localFlag, Boolean.class);
            }
            JSONObject jsonObject = appNameFlag.getJSONObject(appName);
            if (jsonObject == null || jsonObject.isEmpty()) {
                return getGroupValue(entity, tag, key, localFlag, Boolean.class);
            }
            Boolean aBoolean = jsonObject.getBoolean(key);
            if (aBoolean == null || aBoolean) {
                return getGroupValue(entity, tag, key, localFlag, Boolean.class);
            }
            return false;
        } catch (Exception e) {
            log.warn("{},tag:{},key:{},localFlag:{}", e.getMessage(), tag, key, localFlag, e);
        }
        return localFlag;
    }


    private <T> T getGroupValue(RocketMqSwitchEntity entity, String tag, String key, T localVale, Class<T> tClass) {
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

    /**
     * 2025/6/11 01:54
     * 获取其他值的开关
     */
    private <T> T getMsgFlagValue(String tag, String key, T localValue, Class<T> tClass) {
        try {
            RocketMqSwitchEntity entity = marketingCommonConfig.getRocketMqSwitch2();
            if (entity == null) {
                return localValue;
            }
            JSONObject appNameFlag = entity.getAppNameFlag();
            if (null == appNameFlag) {
                return getGroupValue(entity, tag, key, localValue, tClass);
            }
            JSONObject jsonObject = appNameFlag.getJSONObject(appName);
            if (jsonObject == null || jsonObject.isEmpty()) {
                return getGroupValue(entity, tag, key, localValue, tClass);
            }
            T value = jsonObject.getObject(key, tClass);
            if (value == null) {
                return getGroupValue(entity, tag, key, localValue, tClass);
            }
            return value;
        } catch (Exception e) {
            log.warn("{},tag:{},key:{},localFlag:{}", e.getMessage(), tag, key, localValue, e);
        }
        return localValue;
    }

}
