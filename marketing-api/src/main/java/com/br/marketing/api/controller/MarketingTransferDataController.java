package com.br.marketing.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.utils.BrCipherJsonUtils;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.context.RuntimeDataContext;
import com.br.marketing.entity.MonitorTypeEnum;
import com.br.marketing.service.PushRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 营销数据接入接口
 */
@Api(value = "MarketingUserPreController")
@RequestMapping("/marketingTransferData")
@RestController
public class MarketingTransferDataController {

    private static final Logger log = LoggerFactory.getLogger(MarketingTransferDataController.class);

    @Autowired
    PushRuleService pushRuleService;


    @ApiOperation(value = "接收转化数据")
    @PostMapping("/receiveTransferDataSync")
    @LogAnnotation
    public ApiNoDataResult receiveTransferDataSync(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        RuntimeDataContext.getData().setUploadType(MonitorTypeEnum.UPLOAD_TYPE_2.getType());
        RuntimeDataContext.getData().setApiCode(apiCode);
        long l = System.currentTimeMillis();
        RuntimeDataContext.getData().setJsonData(BrCipherJsonUtils.cipherEncodeJsonDataArr(jsonData, Constants.TAG_KEY,Constants.JSON_DATA_KEYARR));
        if (log.isInfoEnabled()) {
            log.info("apiCode:{},接收转化数据加密耗时：{}", apiCode, (System.currentTimeMillis() - l));
        }
        Result result = pushRuleService.insertTransferData(apiCode, jsonData);
        return new ApiNoDataResult().fromResult(result);
    }

    /**
     * 获取转化数据上传详情
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @ApiOperation(value = "获取转化数据上传详情")
    @PostMapping("/getTransferDataStauts")
    public ApiResult getTransferDataStauts(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        try {
            JSONObject jsonObject = JSON.parseObject(jsonData);
            String requestId = jsonObject.getString("requestId");
            return new ApiResult().fromResult(pushRuleService.getTransferDataStatus(apiCode,requestId));
        }  catch (JSONException ex) {
            return new ApiResult()
                    .setCode(MarketingErrorInfo.JSON_DATA_ERROR.getErrorCode())
                    .setMessage(MarketingErrorInfo.JSON_DATA_ERROR.getErrorMsg());

        }
    }


}
