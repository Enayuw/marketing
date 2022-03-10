package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.RetryMainLog;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
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
public class CustomerTransferHandler extends AbstractExternalInterfaceHandler<ConversionData> {

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Override
    public JSONObject call(List<ConversionData> transferList, MarketingTransferInfo transferInfo) {

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

            robotOutboundDTO.setApiCode(transferInfo.getApiCode());
            robotOutboundDTO.setJsonData(new TransferJsonDataDTO(subList));
            robotOutboundDTO.setTransferInfoId(transferInfo.getId());
            if("9999".equals(callCustomerTransfer(robotOutboundDTO).getCode())){
                //调用客户转化接口失败，记录数据入库，定时任务重试
                RetryMainLog mainLog = new RetryMainLog();
                mainLog.setRetryType(1);
                mainLog.setRetryParam(JSON.toJSONString(robotOutboundDTO));
                mainLog.setRetryParamType(robotOutboundDTO.getClass().getName());
                mainLog.setRetryService("customerTransferHandler");
                mainLog.setRetryMethod("callCustomerTransfer");
                mainLog.setRetryNum(0);
                mainLog.setRetryMaxNum(3);
                mainLog.setRetryStatus(1);
                mainLog.setCreateTime(new Date());
                mainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                retryMainLogMapper.insertSelective(mainLog);
            }

        }
        return null;
    }

    /**
     * 调用客户接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     * @param robotOutboundDTO
     * @return
     */
    private TransferRobotOutboundVO<UnsuccessfulData> callCustomerTransfer(TransferRobotOutboundDTO robotOutboundDTO){
        TransferRobotOutboundVO<UnsuccessfulData> transferRobotOutboundVO = robotaiApiServiceClient.pushRobotai(robotOutboundDTO);
        if (!"9999".equals(transferRobotOutboundVO.getCode())){
            List<ConversionData> conversionData = robotOutboundDTO.getJsonData().getConversionData();
            Set<String> set = conversionData.stream().map(ConversionData::getDataId).collect(Collectors.toSet());
            saveBizLog(String.join(",",set),handlerEnum().getCode(),robotOutboundDTO.getTransferInfoId());
        }
        return transferRobotOutboundVO;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum () {
            return InterfaceHandlerEnum.CUSTOMER_TRANSFER;
        }
}
