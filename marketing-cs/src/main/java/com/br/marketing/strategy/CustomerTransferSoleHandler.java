package com.br.marketing.strategy;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundSoleDTO;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.DataJoinLogDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class CustomerTransferSoleHandler extends AbstractExternalInterfaceHandler<ConversionData> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public JSONObject call(List<ConversionData> transferList, ProcessHandlerContext context) {

        /**
         * 客服标准接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = transferList.size();
        String last = context.getLast();
        ArrayList<ConversionData> sendList = new ArrayList<>();
        ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
        Integer sum = 0;
        for (ConversionData conversionData : transferList) {
            sum++;
            sendList.add(conversionData);
            logList.add(methodRetryHandlerService.dataJoinLogFix(conversionData,DistributeTypeEnum.CUSTOMERTRANSFER
                    ,context.getApiCode(), conversionData.getCaseNum(), conversionData.getPhone()
                    , Long.valueOf(conversionData.getDataId()), DistributeSourceTypeEnum.TRANSFER));
            if(sendList.size()==pageSize||sum == totalCount){
                TransferRobotOutboundSoleDTO robotOutboundDTO = new TransferRobotOutboundSoleDTO();
                robotOutboundDTO.setApiCode(context.getApiCode());
                robotOutboundDTO.setTransferInfoId(context.getTransferInfoId());
                robotOutboundDTO.setData(sendList);
                robotOutboundDTO.setLast(sum == totalCount?last:(last != null ? "0" : null));
                methodRetryHandlerService.callCustomerTransfer(robotOutboundDTO, null);
                sendList = new ArrayList<>();
                logList = new ArrayList<>();
            }
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE;
    }
}
