package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.input.*;
import com.br.marketing.context.ProcessHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 针对榕树要把 4004643 转化接口上传的数据，变成 4004733 的数据推送决策
 * 因为之前代码的apiCode从上下文ProcessHandlerContext获取，这导致apiCode替换变得困难
 * 所以重新写了本方法，apiCode数据从新增对象 ConversionDataDTO 中获取（ConversionData中不包含apiCode字段）
 * @Author yu.xia@brgroup.com
 * @Date 2024/11/18 21:06
 */
@Service
@Slf4j
public class CustomerTransferHandlerForRs extends AbstractExternalInterfaceHandler<ConversionDataDTO> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public JSONObject call(List<ConversionDataDTO> transferList, ProcessHandlerContext context) {
        List<ConversionData> conversionDataList = new ArrayList<>();
        // apiCode不从上下文获取，防止规则中修改后不生效
        String apiCode = null;
        // 遍历数据数组
        for (int i=0; i<transferList.size(); i++) {
            ConversionDataDTO conversionDataDTO = transferList.get(i);
            if(i<1){
                apiCode = conversionDataDTO.getApiCode();
            }
            ConversionData conversionData = new ConversionData();
            BeanUtils.copyProperties(conversionDataDTO, conversionData);
            conversionDataList.add(conversionData);
        }
        /**
         * 客服标准接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = conversionDataList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        String last = context.getLast();
        String lastRep;
        for (int i = 1; i <= pageCount; i++) {
            List<ConversionData> subList;
            TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
            if (i == pageCount) {
                subList = conversionDataList.subList((i - 1) * pageSize, totalCount);
                lastRep = last;
            } else {
                subList = conversionDataList.subList((i - 1) * pageSize, pageSize * (i));
                lastRep = last != null ? "0" : null;
            }

            robotOutboundDTO.setApiCode(apiCode);
            robotOutboundDTO.setJsonData(new TransferJsonDataDTO(subList, lastRep));
            robotOutboundDTO.setTransferInfoId(context.getTransferInfoId());

            methodRetryHandlerService.callCustomerTransfer(robotOutboundDTO, 0);
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.CUSTOMER_AUTO_FILTRATION_RS;
    }
}
