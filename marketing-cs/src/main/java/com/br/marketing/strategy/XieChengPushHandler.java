package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.XieChengDataDTO;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * 推送携程
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
            if (i > 0) {
                producter.send("Marketing.Universal.SftpToDb.XieChengReceive", String.valueOf(dto.getInitId()));
            }
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.XIE_CHENG_CALL_RECORD_INSERT_DB;
    }
}
