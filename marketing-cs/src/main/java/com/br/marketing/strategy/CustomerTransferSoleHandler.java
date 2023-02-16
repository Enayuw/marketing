package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundSoleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        String last = context.getLast();
        String lastRep;
        for (int i = 1; i <= pageCount; i++) {
            List<ConversionData> subList;
            TransferRobotOutboundSoleDTO robotOutboundDTO = new TransferRobotOutboundSoleDTO();
            if (i == pageCount) {
                subList = transferList.subList((i - 1) * pageSize, totalCount);
                lastRep = last;
            } else {
                subList = transferList.subList((i - 1) * pageSize, pageSize * (i));
                lastRep = last != null ? "0" : null;
            }

            robotOutboundDTO.setApiCode(context.getApiCode());
            robotOutboundDTO.setTransferInfoId(context.getTransferInfoId());
            robotOutboundDTO.setData(subList);
            robotOutboundDTO.setLast(lastRep);

            methodRetryHandlerService.callCustomerTransfer(robotOutboundDTO, null);
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE;
    }
}
