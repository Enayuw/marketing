package com.br.marketing.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.api.customer.black.service.CustomerBlackDataService;
import com.br.marketing.api.customer.transfer.service.CustomerTransferDataService;
import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.context.RuntimeDataContext;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.entity.MonitorTypeEnum;
import com.br.marketing.service.IPushShuheDataService;
import com.br.marketing.service.PushRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * 营销数据接入接口
 */
@Tag(name = "MarketingTransferDataController", description = "营销数据接入接口")
@RequestMapping("/marketingTransferData")
@RestController
public class MarketingTransferDataController {

    private static final Logger log = LoggerFactory.getLogger(MarketingTransferDataController.class);

    @Autowired
    PushRuleService pushRuleService;

    @Resource
    private IPushShuheDataService iPushShuheDataService;

    @Resource
    private CustomerTransferDataService customerTransferDataService;
    @Resource
    private CustomerBlackDataService customerBlackDataService;



    /**
     * 智能营销标准转化数据上传接口
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @Operation(summary = "接收转化数据")
    @PostMapping("/receiveTransferDataSync")
    @LogAnnotation
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ApiNoDataResult receiveTransferDataSync(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        RuntimeDataContext.getData().setUploadType(MonitorTypeEnum.UPLOAD_TYPE_2.getType());
        RuntimeDataContext.getData().setApiCode(apiCode);
        RuntimeDataContext.getData().setJsonData(jsonData);
        Result result = pushRuleService.insertTransferData(apiCode, jsonData);
        return new ApiNoDataResult().fromResult(result);
    }

    /**
     * 智能营销标准转化数据查询接口
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @Operation(summary = "获取转化数据上传详情")
    @PostMapping("/getTransferDataStauts")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ApiResult getTransferDataStauts(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        try {
            JSONObject jsonObject = JSON.parseObject(jsonData);
            String requestId = jsonObject.getString("requestId");
            return new ApiResult().fromResult(pushRuleService.getTransferDataStatus(apiCode, requestId), null);
        } catch (JSONException ex) {
            return new ApiResult()
                    .setCode(MarketingErrorInfo.JSON_DATA_ERROR.getErrorCode())
                    .setMessage(MarketingErrorInfo.JSON_DATA_ERROR.getErrorMsg());

        }
    }


    /**
     * 智能营销数禾（客户）订制转化数据上传接口
     *
     * @param apiCode  apiCode
     * @param jsonData 业务数据json结构
     * @return ApiNoDataResult 业务响应
     */
    @Operation(summary = "接收数禾订制转化数据")
    @PostMapping("receiveShuheTransferDataSync")
    @LogAnnotation
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ResponseCustomDTO receiveShuheTransferDataSync(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        RuntimeDataContext.getData().setUploadType(MonitorTypeEnum.UPLOAD_TYPE_2.getType());
        RuntimeDataContext.getData().setApiCode(apiCode);
        RuntimeDataContext.getData().setJsonData(jsonData);
        return iPushShuheDataService.saveShuheTransferDataTwoVersion(apiCode, jsonData);
    }


    /**
     * 订制转化数据上传接口
     *
     * @param apiCode  apiCode
     * @param jsonData 业务数据json结构
     * @return ResponseCustomDTO 业务响应
     */
    @Operation(summary = "订制转化数据上传接口")
    @PostMapping("receiveTransferData")
    @LogAnnotation
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ResponseCustomDTO receiveTransferData(@RequestParam("apiCode") String apiCode
            , @RequestParam("jsonData") String jsonData) {
        RuntimeDataContext.getData().setUploadType(MonitorTypeEnum.UPLOAD_TYPE_2.getType());
        RuntimeDataContext.getData().setApiCode(apiCode);
        RuntimeDataContext.getData().setJsonData(jsonData);
        return customerTransferDataService.receiveTransferDataHandler(apiCode, jsonData);
    }


    /**
     * 接收客制化黑名单数据
     *
     * @param apiCode apiCode
     * @param jsonData json数据
     * @return {@link ResponseCustomDTO }
     * @author senyang.zheng
     * @date 2024/08/06
     */
    @Operation(summary = "定制黑名单数据接入接口")
    @PostMapping("/receiveCustomizeBlackData")
    @LogAnnotation
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ResponseCustomDTO receiveCustomizeBlackData(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        RuntimeDataContext.getData().setUploadType(MonitorTypeEnum.UPLOAD_TYPE_2.getType());
        RuntimeDataContext.getData().setApiCode(apiCode);
        RuntimeDataContext.getData().setJsonData(jsonData);
        return customerBlackDataService.receiveCustomizeBlackData(apiCode, jsonData);
    }


}
