package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.DaasAndConversionData;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.ProcessHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 推送人工实时推送用户名单(单条)与外呼转化 有去重功能
 *
 * @author zeqiang.guo
 * @dateTime 2023/08/23 17:13
 */
@Service
@Slf4j
public class ArtificialRealTimeUserAndCustomerTransferSoleFacade extends AbstractExternalInterfaceHandler<DaasAndConversionData> {

    @Resource
    private ArtificialRealTimeUserDataSoleHandler artificialRealTimeUserDataSoleHandler;

    @Resource
    private CustomerTransferSoleHandler customerTransferSoleHandler;

    @Override
    public JSONObject call(List<DaasAndConversionData> transferData, ProcessHandlerContext context) {
        List<RealTimeUserDataSoleDTO> realTimeUserDataSoleDTOList = new ArrayList<>();
        List<ConversionData> conversionDataList = new ArrayList<>();
        for (DaasAndConversionData daasAndConversionData : transferData) {
            realTimeUserDataSoleDTOList.add(daasAndConversionData.getRealTimeUserDataSoleDTO());
            conversionDataList.add(daasAndConversionData.getConversionData());
        }
        try {
            if (realTimeUserDataSoleDTOList.size() > 0) {
                artificialRealTimeUserDataSoleHandler.call(realTimeUserDataSoleDTOList, context);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), e.getMessage()), e);
        }
        if (conversionDataList.size() > 0) {
            customerTransferSoleHandler.call(conversionDataList, context);
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA_AND_CUSTOMER_TRANSFER_SOLE;
    }
}
