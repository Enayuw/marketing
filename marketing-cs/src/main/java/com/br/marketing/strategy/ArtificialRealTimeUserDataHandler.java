package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
/**
 * @Description : 人工实时推送用户名单(单条)处理
 * @Author : lizhen
 * @Date : Create in 2022/03/17 16:11
 */
public class ArtificialRealTimeUserDataHandler extends AbstractExternalInterfaceHandler<RealTimeUserDataDTO> {

    @Autowired
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    JSONObject call(List<RealTimeUserDataDTO> transferData, ProcessHandlerContext context) {
        for (RealTimeUserDataDTO realTimeUserDataDTO : transferData) {
            PhoneSaleExtendInfo phoneSaleExtendInfo = realTimeUserDataDTO.getPhoneSaleExtendInfo();
            //插入b_phone_sale_extend_info
            Date date = new Date();
            phoneSaleExtendInfo.setCreateTime(date);
            phoneSaleExtendInfoMapper.insertSelective(phoneSaleExtendInfo);
            //调用Dass
            DassSingleImportAdapDTO dassImportAdapDTO = realTimeUserDataDTO.getDassSingleImportAdapDTO();
            dassImportAdapDTO.setExtendInfo(phoneSaleExtendInfo.getId().toString());
            dassImportAdapDTO.setTransferInfoId(context.getTransferInfoId());
            methodRetryHandlerService.callDassRealTimeUserData(dassImportAdapDTO, 0);
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA;
    }
}
