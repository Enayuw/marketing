package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.transfer.ConversionDataSoleDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundSoleDTO;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.es.util.BrCipherMaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class CustomerTransferSoleUseStatusHandler extends AbstractExternalInterfaceHandler<ConversionDataSoleDTO> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public JSONObject call(List<ConversionDataSoleDTO> dataSoles, ProcessHandlerContext context) {

        /**
         * 客服标准接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = dataSoles.size();
        String last = context.getLast();
        //数据数组
        ArrayList<ConversionData> sendList = new ArrayList<>();
        //数据日志数组
        ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
        int sum = 0;
        long start = System.currentTimeMillis();
        // 遍历数据数组
        for (ConversionDataSoleDTO soleDTO : dataSoles) {
            ConversionData conversionData = soleDTO.getConversionData();
            sum++;
            sendList.add(conversionData);
            // 把封装的日志插入到数组中
            logList.add(methodRetryHandlerService.dataJoinLogFix(conversionData, DistributeTypeEnum.CUSTOMERTRANSFER
                    , context.getApiCode()
                    , conversionData.getCaseNum()
                    , BrCipherMaker.getInstance().encode(conversionData.getPhone())
                    , Long.valueOf(conversionData.getDataId())
                    , conversionData.getDistributeSourceTypeEnum() == null
                            ? DistributeSourceTypeEnum.TRANSFER : conversionData.getDistributeSourceTypeEnum()
                    , soleDTO.getStatus()
                    , conversionData.getExpireEndDate()));
            if (sendList.size() == pageSize || sum == totalCount) {
                // 对象继承 DataDistributeLogBase
                TransferRobotOutboundSoleDTO robotOutboundDTO = new TransferRobotOutboundSoleDTO();
                robotOutboundDTO.setApiCode(context.getApiCode());
                robotOutboundDTO.setTransferInfoId(context.getTransferInfoId());
                robotOutboundDTO.setData(sendList);
                robotOutboundDTO.setDetailLogList(logList);
                if(sum == totalCount){
                    robotOutboundDTO.setLast(last);
                }else {
                    robotOutboundDTO.setLast(last != null ? "0" : null);
                }
                //传参去重
                //去重字段维度,根据传入值赋值，默认为cell维度去重
                if (conversionData.getSoleField() != null) {
                    robotOutboundDTO.setSoleField(conversionData.getSoleField());
                } else {
                    robotOutboundDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
                }
                //去重范围,根据传入值赋值，默认当天去重
                if (conversionData.getSoleType() != null) {
                    robotOutboundDTO.setSoleDay(conversionData.getSoleType());
                } else {
                    robotOutboundDTO.setSoleDay(1);
                }
                robotOutboundDTO.setIsSole(true);
                methodRetryHandlerService.callCustomerTransfer(robotOutboundDTO, null);
                sendList = new ArrayList<>();
                logList = new ArrayList<>();
            }
        }
        log.warn("推送客服转化使用状态自定义去重耗时：" + (System.currentTimeMillis() - start));
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE_USE_STATUS;
    }
}
