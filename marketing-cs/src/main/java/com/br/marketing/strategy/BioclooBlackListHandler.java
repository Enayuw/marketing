package com.br.marketing.strategy;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.biocloo.BioclooClient;
import com.br.marketing.client.biocloo.input.BlackDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BioclooBlackListHandler extends AbstractExternalInterfaceHandler<BlackDataDTO.DataDTO> {

    @Resource
    private BioclooClient bioclooClient;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public JSONObject call(List<BlackDataDTO.DataDTO> blackDataDTOList, ProcessHandlerContext context) {
        int pageSize = 500;
        int totalCount = blackDataDTOList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<BlackDataDTO.DataDTO> subList;
            if (i == pageCount) {
                subList = blackDataDTOList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = blackDataDTOList.subList((i - 1) * pageSize, pageSize * (i));
            }
            BlackDataDTO dto = new BlackDataDTO();
            dto.setMethod("blackData");
            dto.setData(subList);
            JSONObject proxyJson = marketingCommonConfig.getShuHeProxyToBioclooApiCode();
            dto.setApiCode(proxyJson.getString(context.getApiCode()));
            Result callBalckResult = bioclooClient.pushBlackDataToBiocloo(dto, 0);
            if (!ResultCode.SUCCESS.getValue().equals(callBalckResult.getCode())) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUHE_INTERFACEERROR.getCode(),
                    String.format("推送百可录黑名单报错：%s", callBalckResult.getData())));
            }
        }
        return null;
    }

    /**
     * 按照三方接口逻辑调用接口
     */
    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.BIOCLOO_BLACK_LIST;
    }
}
