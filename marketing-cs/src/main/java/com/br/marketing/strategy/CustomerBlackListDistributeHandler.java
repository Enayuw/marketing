package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.client.robotaiapi.input.BlackPhoneDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneParentDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @Description : 推送客服黑名单Handler(支持一个apicode分发到多个apicode)
 * ---------------------------------
 * @Author : hong.chen
 * @Date : Create in 2022/5/19 18:08
 */
@Slf4j
@Service
public class CustomerBlackListDistributeHandler extends AbstractExternalInterfaceHandler<BlackDetailDTO> {
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public JSONObject call(List<BlackDetailDTO> blackDetailDTOList, ProcessHandlerContext context) {
        if (context == null) {
            return null;
        }

        List<String> apiCodes = marketingCommonConfig.getCustomerBlackListApiCodes().get(context.getApiCode());
        for (String apiCode : apiCodes) {
            realAction(blackDetailDTOList, context, null, apiCode);
        }
        return null;
    }

    private void realAction(List<BlackDetailDTO> blackDetailDTOList, ProcessHandlerContext context, String type, String apiCode) {
        /**
         * 客服黑名单接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = blackDetailDTOList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<BlackDetailDTO> subList;
            if (i == pageCount) {
                subList = blackDetailDTOList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = blackDetailDTOList.subList((i - 1) * pageSize, pageSize * (i));
            }
            BlackPhoneDTO<BlackDetailDTO> jsondata = new BlackPhoneDTO<>();
            jsondata.setMethod("blackData");
            jsondata.setData(subList);
            ReqBlackPhoneDTO dto = new ReqBlackPhoneDTO();
            dto.setApiCode(apiCode);
            dto.setJsonData(JSON.toJSONString(jsondata));
            ReqBlackPhoneParentDTO parentDTO = new ReqBlackPhoneParentDTO();
            parentDTO.setDto(dto);
            parentDTO.setBlackDetailDTOList(subList);
            parentDTO.setTransferInfoId(context.getTransferInfoId());
            parentDTO.setExtendInfo(type);
            Result<String> callBalckResult = methodRetryHandlerService.callCustomerBlack(parentDTO, 0);
            if (!ResultCode.SUCCESS.getValue().equals(callBalckResult.getCode())) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                        String.format("推送黑名单报错：%s", callBalckResult.getData())));
            }
        }
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.CUSTOMER_BLACKLIST_DISTRIBUTE;
    }
}
