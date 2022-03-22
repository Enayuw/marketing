package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.ProcessHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Override
    public JSONObject call(List<ConversionData> transferList, ProcessHandlerContext context) {

        /**
         * 客服标准接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = transferList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<ConversionData> subList = new ArrayList<>();
            TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
            if (i == pageCount) {
                subList = transferList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = transferList.subList((i - 1) * pageSize, pageSize * (i));
            }

            robotOutboundDTO.setApiCode(context.getApiCode());
            robotOutboundDTO.setJsonData(new TransferJsonDataDTO(subList));
            robotOutboundDTO.setTransferInfoId(context.getTransferInfoId());

            callCustomerTransfer(robotOutboundDTO,0);
        }
        return null;
    }

    /**
     * 调用客户接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     * @param robotOutboundDTO
     * @return
     */
    @RetryMethod
    public Result<TransferRobotOutboundVO<UnsuccessfulData>> callCustomerTransfer(TransferRobotOutboundDTO robotOutboundDTO,Integer retry){
        TransferRobotOutboundVO<UnsuccessfulData> transferRobotOutboundVO = robotaiApiServiceClient.pushRobotai(robotOutboundDTO);
        if (!"9999".equals(transferRobotOutboundVO.getCode())){
            List<ConversionData> conversionData = robotOutboundDTO.getJsonData().getConversionData();
            Set<String> set = conversionData.stream().map(ConversionData::getDataId).collect(Collectors.toSet());
            saveBizLog(String.join(",",set),handlerEnum().getCode(),robotOutboundDTO.getTransferInfoId());
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(transferRobotOutboundVO);
        }
        log.error("调用客服接口失败 -- {}",JSON.toJSONString(transferRobotOutboundVO));
        //调用客户转化接口失败，记录数据入库，定时任务重试
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(transferRobotOutboundVO);
    }

    @Override
    public InterfaceHandlerEnum handlerEnum () {
            return InterfaceHandlerEnum.CUSTOMER_TRANSFER;
        }
}
