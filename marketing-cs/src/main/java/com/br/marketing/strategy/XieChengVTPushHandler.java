package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.XieChengDataDTO;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;
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
    private MarketingCommonConfig marketingCommonConfig;

    private static final String EXPIRE_TIME = "3600000";

    @Override
    JSONObject call(List<XieChengDataDTO> list, ProcessHandlerContext context) {
        String expireTime = StringUtils.hasText(marketingCommonConfig.getMessageQueueExpireTime())
                ? marketingCommonConfig.getMessageQueueExpireTime() : EXPIRE_TIME;

        for (XieChengDataDTO dto : list) {
            Boolean toDelay = dto.getToDelay();
            if (!toDelay) {
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
                    producter.send(ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGRECEIVE
                            , msg.toJSONString());
                }
            } else {
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("apiCode",context.getApiCode());
//                jsonObject.put("ids",list.stream().map(XieChengDataDTO::getInitId).collect(Collectors.toSet()));
//                jsonObject.put("tcId",handlerService.getTcIdFromRedis(context.getApiCode()));

                MqFact mqFact = new MqFact();
                mqFact.setSourceId(context.getTransferInfoId());
                mqFact.setIsDelay(1);
                Set set = new HashSet<>();
                set.add("XieCheng_CallRecord_Insert_DB_VT");
                mqFact.setIncludeRules(set);
//                mqFact.setMessage(jsonObject.toJSONString());
                mqFact.setSource(TransferSource.TRANSFER_DATA_SET_PROCESS.getCode());
                String message = JSON.toJSONString(mqFact);
                producter.sendByExpiration(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE_DELAY, message, expireTime);
            }
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.XIE_CHENG_CALL_RECORD_INSERT_DB_VT;
    }
}
