package com.br.marketing.datarelayservice.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.UMengCryptoUtil;
import com.br.marketing.service.Impl.umeng.IUMengDataCallbackService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Tag(name = "友盟智能时机业务接口", description = "友盟智能时机业务接口")
@RequestMapping("/marketing/v1/umeng")
@RestController
@Slf4j
public class UMengCustomizeController {

    @Resource
    private IUMengDataCallbackService umengCustomizeService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Operation(summary = "友盟智能时机回调")
    @PostMapping("/marketingCallback")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public Result marketingCallback(@RequestBody String requestBody, HttpServletRequest request) {
        if (!checkHeaderSign(request)) {
            return new Result().failure().setMessage("header sign error");
        }
        return umengCustomizeService.marketingCallback(requestBody,request);
    }

    private boolean checkHeaderSign(HttpServletRequest request) {
        boolean result = false;
        String headerBizId = request.getHeader("bizid");
        String speedBizeId = marketingCommonConfig.getUMengBizInfoMap().get("bizId");
        if (headerBizId != null && headerBizId.equals(speedBizeId)){
            result = true;
        }else {
            log.warn("uMeng headerCheckError,headerBizId:{},speedBizId:{}",headerBizId,speedBizeId);
        }
        return result;
    }


}
