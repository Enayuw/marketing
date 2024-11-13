package com.br.marketing.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.SnowflakeIdGenerator;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.rocketmq.RocketMqSwitchEntity;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
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
     * mq消息头 生产消息的唯一标识
     */
    public static final String UUID_KEY = "uuid";

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private RocketMqTemplate template;

    public Boolean rocketMQSwitchFlag(String apiCode, String tag){
        UUID uuid = UUID.randomUUID();
        if(log.isInfoEnabled()){
            log.info("[{}]rocketMQSwitchFlag--apiCode[{}]tag[{}]", uuid, apiCode, tag);
        }
        String rocketMqSwitchString = marketingCommonConfig.getRocketMqSwitch2();
        if(StringUtils.isBlank(rocketMqSwitchString)){
            return Boolean.FALSE;
        }
        RocketMqSwitchEntity entity = JSON.parseObject(rocketMqSwitchString, new TypeReference<RocketMqSwitchEntity>() {
        }.getType());
        String global = entity.getGlobal();
        if("true".equalsIgnoreCase(global)){
            return Boolean.TRUE;
        }else{
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
        }
    }


    public Boolean rocketLogSwitchFlag(String tag){
        UUID uuid = UUID.randomUUID();
        if(log.isInfoEnabled()){
            log.info("[{}]rocketLogSwitchFlag--tag[{}]", uuid, tag);
        }
        String rocketMqSwitchString = marketingCommonConfig.getRocketMqSwitch2();
        if(StringUtils.isBlank(rocketMqSwitchString)){
            return Boolean.FALSE;
        }
        RocketMqSwitchEntity entity = JSON.parseObject(rocketMqSwitchString, new TypeReference<RocketMqSwitchEntity>() {
        }.getType());
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

    public SendResult syncSend(String topic, String tags, String msg){
        Message<String> build = MessageBuilder.withPayload(msg)
                .setHeader(UUID_KEY, snowflakeIdGenerator.nextId())
                .build();
        return template.syncSendMessage(topic, tags, build);
    }

    public SendResult syncSendDelaySecond(String topic, String tags, String msg, long delayTime){
        Message<String> build = MessageBuilder.withPayload(msg)
                .setHeader(UUID_KEY, snowflakeIdGenerator.nextId())
                .build();
        return template.syncSendDelaySecond(topic, tags, build, delayTime);
    }

}
