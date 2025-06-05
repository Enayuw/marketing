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
import java.util.UUID;


/**
 * RocketMQ和RabbitMQ切换开关
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
     * mq消息头 生产消息的唯一标识
     */
    public static final String UUID_KEY = "uuid";

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

    public Boolean rocketMQSwitchFlag(String apiCode, String tag){
        try {
            UUID uuid = UUID.randomUUID();
            if(log.isInfoEnabled()){
                log.info("[{}]rocketMQSwitchFlag--apiCode[{}]tag[{}]", uuid, apiCode, tag);
            }
            RocketMqSwitchEntity entity = marketingCommonConfig.getRocketMqSwitch2();
            if(entity == null){
                return Boolean.FALSE;
            }
            String global = entity.getGlobal();
            if("false".equalsIgnoreCase(global)){
                return Boolean.FALSE;
            }else if("true".equalsIgnoreCase(global)){
                JSONObject group = entity.getGroup();
                JSONObject tagObjet = group.getJSONObject(tag);
                if(null == tagObjet || tagObjet.isEmpty()){
                    return Boolean.FALSE;
                }else{
                    Boolean flagBoolean = tagObjet.getBoolean(FLAG);
                    if(log.isInfoEnabled()){
                        log.info("[{}]rocketMQSwitchFlag--tagObjet[{}]flagBoolean[{}]", uuid, tagObjet, flagBoolean);
                    }
                    if(flagBoolean){
                        return Boolean.TRUE;
                    }else{
                        if(StringUtils.isBlank(apiCode)){
                            return Boolean.FALSE;
                        }
                        String apiCodes = tagObjet.getString(APICODES_SPEED);
                        if(StringUtils.isNotBlank(apiCodes) && apiCodes.contains(apiCode)){
                            return Boolean.TRUE;
                        }
                    }
                }
                return Boolean.FALSE;
            }else{
                return Boolean.FALSE;
            }
        }catch (Exception e){
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

    public boolean rocketLogSwitchFlag(String tag){
        UUID uuid = UUID.randomUUID();
        if(log.isInfoEnabled()){
            log.info("[{}]rocketLogSwitchFlag--tag[{}]", uuid, tag);
        }
        RocketMqSwitchEntity entity = marketingCommonConfig.getRocketMqSwitch2();
        if(entity == null){
            return Boolean.FALSE;
        }
        JSONObject group = entity.getGroup();
        JSONObject tagObjet = group.getJSONObject(tag);
        if(null == tagObjet || tagObjet.isEmpty()){
            return Boolean.FALSE;
        }else{
            Boolean logBoolean = tagObjet.getBoolean(PRINT_LOG);
            if(log.isInfoEnabled()){
                log.info("[{}]rocketLogSwitchFlag--tagObjet[{}]logBoolean[{}]", uuid, tagObjet, logBoolean);
            }
            if(null != logBoolean && logBoolean){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public <T> void rocketLogSwitchFlag(String tag, MessageExt messageExt, T t, long startTimeMillis) {
        if(rocketLogSwitchFlag(tag)){
            log.warn("rocketLogSwitchFlag--耗时[{}ms]--tag[{}];message[{}];messageExt[{}]"
                    , (System.currentTimeMillis() - startTimeMillis), tag, t, messageExt);
        }
    }

    public SendResult syncSend(String topic, String tags, String msg) {
        Message<String> build;
        if (msgUUIdFlag(tags)) {
            build = MessageBuilder.withPayload(msg)
                    .setHeader(UUID_KEY, messageIdempotentHandler.generateMessageId())
                    .build();
        } else {
            build = MessageBuilder.withPayload(msg).build();
        }
        return template.syncSendMessage(topic, tags, build);
    }

    public SendResult syncSendDelaySecond(String topic, String tags, String msg, long delayTime){
        Message<String> build = MessageBuilder.withPayload(msg)
                .setHeader(UUID_KEY, messageIdempotentHandler.generateMessageId())
                .build();
        return template.syncSendDelaySecond(topic, tags, build, delayTime);
    }


    public boolean msgUUIdFlag(String tags) {
        if (msgIdemFlag(tags)) {
            return Boolean.TRUE;
        }
        return getMsgFlag(tags, MSG_UUID_FLAG);
    }

    public boolean msgIdemFlag(String tags) {
        return getMsgFlag(tags, MSG_IDEM_FLAG);
    }

    private boolean getMsgFlag(String tags, String key) {
        RocketMqSwitchEntity entity = marketingCommonConfig.getRocketMqSwitch2();
        if (entity == null) {
            return Boolean.TRUE;
        }
        JSONObject appNameFlag = entity.getAppNameFlag();
        if (null == appNameFlag) {
            return Boolean.TRUE;
        }
        JSONObject jsonObject = appNameFlag.getJSONObject(appName);
        if (jsonObject == null || jsonObject.isEmpty()) {
            return Boolean.TRUE;
        }
        Boolean aBoolean = jsonObject.getBoolean(key);
        if (aBoolean == null || aBoolean) {
            JSONObject group = entity.getGroup();
            if (group == null || group.isEmpty()) {
                return Boolean.TRUE;
            }
            JSONObject tagObjet = group.getJSONObject(tags);
            if (tagObjet == null || tagObjet.isEmpty()) {
                return Boolean.TRUE;
            }
            Boolean msgUUIdFlag = tagObjet.getBoolean(key);
            return msgUUIdFlag == null || msgUUIdFlag;
        }
        return Boolean.FALSE;
    }

}
