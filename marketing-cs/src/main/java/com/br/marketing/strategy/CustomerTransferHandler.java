package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.context.ProcessHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 客服转化接口处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/2/28 18:03
 */

@Service
@Slf4j
public class CustomerTransferHandler extends AbstractExternalInterfaceHandler<ConversionData> {

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
            TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
            if (i == pageCount) {
                subList = transferList.subList((i - 1) * pageSize, totalCount);
                lastRep = last;
            } else {
                subList = transferList.subList((i - 1) * pageSize, pageSize * (i));
                lastRep = last != null ? "0" : null;
            }

            robotOutboundDTO.setApiCode(context.getApiCode());
            robotOutboundDTO.setJsonData(new TransferJsonDataDTO(subList, lastRep));
            robotOutboundDTO.setTransferInfoId(context.getTransferInfoId());

            methodRetryHandlerService.callCustomerTransfer(robotOutboundDTO, 0);
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER;
    }
}
