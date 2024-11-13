package com.br.marketing.strategy;

import static com.br.marketing.common.utils.MQConstants.ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.XieChengDataDTO;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;

/**
 * 推送携程(3710058/3710078)
 *
 * @author Guo Zeqiang
 * @dateTime 2022/12/1 16:53
 */
@Service
public class XieChengPushHandler extends AbstractExternalInterfaceHandler<XieChengDataDTO> {

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

    @Override
    JSONObject call(List<XieChengDataDTO> list, ProcessHandlerContext context) {
        for (XieChengDataDTO dto : list) {
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
                    producter.send(ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE, msg.toJSONString());
                }
            }
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.XIE_CHENG_CALL_RECORD_INSERT_DB;
    }
}
