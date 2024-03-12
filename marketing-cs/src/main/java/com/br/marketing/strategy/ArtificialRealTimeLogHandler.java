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

public class ArtificialRealTimeLogHandler extends AbstractExternalInterfaceHandler<RealTimeUserDataDTO> {


    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    JSONObject call(List<RealTimeUserDataDTO> transferData, ProcessHandlerContext context) {
        for (RealTimeUserDataDTO realTimeUserDataDTO : transferData) {
            methodRetryHandlerService.callDassRealTimeLog(realTimeUserDataDTO, 0);
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_LOG;
    }
}
