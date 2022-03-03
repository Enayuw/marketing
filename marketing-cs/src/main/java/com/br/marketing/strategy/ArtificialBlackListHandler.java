package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.PushBlackListResponse;
import com.br.marketing.client.dassservice.input.black.BlackListDTO;
import com.br.marketing.client.dassservice.output.DassExportAdapterDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.RetryMainLog;
import com.br.marketing.mapper.RetryMainLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @Description : 人工黑名单接口处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/2/28 18:12
 */
@Service
public class ArtificialBlackListHandler extends AbstractExternalInterfaceHandler<BlackListDTO>{
    @Resource
    private DassServiceClient dassServiceClient;

    @Override
    public JSONObject call(List<BlackListDTO> transferList, MarketingTransferInfo marketingTransferInfo) {
        System.out.println("获取人工黑名单数据 : "+ JSON.toJSONString(transferList));

        /**
         * 黑名单接口 每1000条数据一个批次
         */
        int pageSize = 1000;
        int totalCount = transferList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<BlackListDTO> subList = new ArrayList<>();
            if (i == pageCount) {
                subList = transferList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = transferList.subList((i - 1) * pageSize, pageSize * (i));
            }

            DassExportAdapterDTO dassExportAdapterDTO = new DassExportAdapterDTO(subList);
            if(!callBlackList(dassExportAdapterDTO)){
                RetryMainLog mainLog = new RetryMainLog();
                mainLog.setRetryType(1);
                mainLog.setRetryParam(JSON.toJSONString(dassExportAdapterDTO));
                mainLog.setRetryParamType(dassExportAdapterDTO.getClass().getName());
                mainLog.setRetryService("artificialBlackListHandler");
                mainLog.setRetryMethod("callBlackList");
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
     * 全局重试任务执行类
     * @param dassExportAdapterDTO
     * @return
     */
    public boolean callBlackList(DassExportAdapterDTO dassExportAdapterDTO){
        List<BlackListDTO> list = dassExportAdapterDTO.getList();
        Result<PushBlackListResponse> pushBlackListResponseResult =
                dassServiceClient.postBlackList(list);
        // 调用接口成功
        boolean flag = ResultCode.SUCCESS.getValue().equals(pushBlackListResponseResult.getCode());
        if (flag){
            Set<String> set = list.stream().map(BlackListDTO::getDataId).collect(Collectors.toSet());
            saveBizLog(String.join(",",set),handlerEnum().getCode());
        }
        return flag;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_BLACK_LIST;
    }
}
