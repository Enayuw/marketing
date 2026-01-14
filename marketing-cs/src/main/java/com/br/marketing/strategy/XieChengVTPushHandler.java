package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.constants.rocketmq.MarketingDelayedConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.XieChengDataDTO;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.br.marketing.common.utils.MQConstants.ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE;

/**
 * 推送携程(3710090/3710091)
 * @author chenh
 * @dateTime 2023/09/15 16:53
 */
@Service
public class XieChengVTPushHandler extends AbstractExternalInterfaceHandler<XieChengDataDTO> {

    @Resource
    private XieChengDataMapper xieChengDataMapper;

    @Resource
    private RabbitMqProducter producter;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private static final String EXPIRE_TIME = "3600000";
    @Resource
    private SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;

    @Override
    JSONObject call(List<XieChengDataDTO> list, ProcessHandlerContext context) {
        String expireTime = StringUtils.hasText(marketingCommonConfig.getMessageQueueExpireTime())
                ? marketingCommonConfig.getMessageQueueExpireTime() : EXPIRE_TIME;

        for (XieChengDataDTO dto : list) {
            Boolean toDelay = dto.getToDelay();
            if (!toDelay) {
                // 推送至携程队列
                XieChengData xieChengData = dto.getXieChengData();
                xieChengData.setCreateTime(new Date());
                xieChengData.setCreateDate(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
                xieChengData.setLocalId(dto.getInitId());
                xieChengData.setPushStatus(1);
                xieChengData.setStatus(1);
                xieChengData.setType("1");
                int i = xieChengDataMapper.insertSelective(xieChengData);
                if (i > 0 && Boolean.FALSE.equals(marketingCommonConfig.getXieChengCallingRecordSwitch())) {
                    JSONObject msg = new JSONObject();
                    msg.put("localId", dto.getInitId());
                    msg.put("type", 2);
                    if(rocketMqSwitch.rocketMQSwitchFlag(xieChengData.getApiCode()
                            , MarketingAssistConstants.TAG_MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE)){
                        rocketMqSwitch.syncSend(MarketingAssistConstants.TOPIC
                                , MarketingAssistConstants.TAG_MARKETING_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE, msg.toJSONString());
                    }else{
                        producter.send(ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE
                                , msg.toJSONString());
                    }
                }
            } else {
                // 推送至延迟队列
                MqFact mqFact = new MqFact();
                mqFact.setSourceId(context.getTransferInfoId());
                mqFact.setIsDelay(1);
                Set set = new HashSet<>();
                set.add("XieCheng_CallRecord_Insert_DB_VT");
                mqFact.setIncludeRules(set);
                mqFact.setSource(TransferSource.CUSTOMER_CALL_RECORD.getCode());
                mqFact.setIdempotentKey(snowflakeRedisGeneratorHandle.nextId());
                String message = JSON.toJSONString(mqFact);
                if(rocketMqSwitch.rocketMQSwitchFlag(null, MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR)){
                    rocketMqSwitch.syncSendDelaySecond(MarketingDelayedConstants.TOPIC
                            , MarketingDelayedConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE_DELAY_HALFHOUR, message
                            , Integer.parseInt(expireTime)/1000);
                }else{
                    producter.sendByExpiration(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE_DELAY, message, expireTime);
                }
            }
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.XIE_CHENG_CALL_RECORD_INSERT_DB_VT;
    }
}
