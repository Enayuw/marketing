package com.br.marketing.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.common.annoation.SaveLog;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.exception.CommonException;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.common.utils.BrCipherJsonUtils;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.context.RuntimeDataContext;
import com.br.marketing.dto.MarketingPreUserSyncStatusDTO;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.entity.MonitorTypeEnum;
import com.br.marketing.service.IPushShuheDataService;
import com.br.marketing.service.PushRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * 营销数据接入接口
 */
@Api(value = "MarketingUserPreController")
@RequestMapping("/marketingUserPre")
@RestController
public class MarketingUserPreController {

    private static final Logger log = LoggerFactory.getLogger(MarketingUserPreController.class);

    @Autowired
    PushRuleService pushRuleService;

    @Resource
    private IPushShuheDataService iPushShuheDataService;


    /**
     * 智能营销数据落库接口
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @ApiOperation(value = "批量接入营销人员数据")
    @PostMapping("/receiveMarketingPreUser")
    @LogAnnotation
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ApiNoDataResult receiveMarketingPreUserSync(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        RuntimeDataContext.getData().setUploadType(MonitorTypeEnum.UPLOAD_TYPE_1.getType());
        RuntimeDataContext.getData().setApiCode(apiCode);
        long l = System.currentTimeMillis();
        RuntimeDataContext.getData().setJsonData(BrCipherJsonUtils.cipherEncodeJsonDataArr(jsonData, Constants.TAG_KEY, Constants.JSON_DATA_KEYARR));
        if (log.isInfoEnabled()) {
            log.info("apiCode:{},批量接入营销人员数据加密耗时：{}", apiCode, (System.currentTimeMillis() - l));
        }
        Result result = pushRuleService.insertMarketingPreUserText(apiCode, jsonData);
        return new ApiNoDataResult().fromResult(result);
    }

    /**
     * 智能营销转化数据接口
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @ApiOperation(value = "萨摩耶转化人员接口")
    @PostMapping("/transferUser")
    @SaveLog
    @LogAnnotation
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ApiNoDataResult transferUser(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        RuntimeDataContext.getData().setUploadType(MonitorTypeEnum.UPLOAD_TYPE_2.getType());
        RuntimeDataContext.getData().setApiCode(apiCode);
        RuntimeDataContext.getData().setJsonData(jsonData);
        Result result = pushRuleService.insertBatchTransferUser(apiCode, jsonData);
        return new ApiNoDataResult().fromResult(result);
    }

    /**
     * 智能营销数据落库查询接口
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @ApiOperation(value = "获取营销人员数据状态")
    @PostMapping("/getMarketingPreUserStauts")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ApiResult getMarketingPreUserStauts(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        try {
            MarketingPreUserSyncStatusDTO o = JSON.parseObject(jsonData,
                    new TypeReference<MarketingPreUserSyncStatusDTO>() {
                    }.getType());
            o.setApiCode(apiCode);
            return new ApiResult().fromResult(pushRuleService.getMarketingPreUserSyncStatus(o),null);
        } catch (CommonException ex) {
            log.error(ex.getMessage());
            MarketingErrorInfo info = ex.getInfo();
            return new ApiResult().setCode(info.getErrorCode()).setMessage(info.getErrorMsg());
        } catch (JSONException ex) {
            return new ApiResult()
                    .setCode(MarketingErrorInfo.JSON_DATA_ERROR.getErrorCode())
                    .setMessage(MarketingErrorInfo.JSON_DATA_ERROR.getErrorMsg());

        }
    }

    @GetMapping("/syncConsumer")
    public Result syncConsumer(@RequestParam("infoId") Long infoId) {
        return pushRuleService.insertMarketingPreUserSync(infoId);
    }

    /**
     * 查询客户信息接口
     *
     * @param cid
     * @param apiCode
     * @param custNum
     * @return
     */
    @ApiOperation(value = "查询客户信息接口")
    @PostMapping("/queryCustInfo")
    public Result queryCustInfo(@RequestParam(required = false) String cid,
                                @RequestParam(required = false) String apiCode,
                                @RequestParam(required = true) String custNum) {
        try {
            return pushRuleService.queryCustInfo(cid, apiCode, custNum);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
    }

    /**
     * 数禾订制版上传数据接口
     *
     * @param apiCode  客户编号
     * @param jsonData 业务数据
     * @author Guo Zeqiang
     * @dateTime 2022/8/28 9:55
     */
    @ApiOperation(value = "数禾订制版上传数据接口")
    @PostMapping("/receiveShuHeUploadData")
    @LogAnnotation
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ResponseCustomDTO receiveShuHeUploadData(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        RuntimeDataContext.getData().setUploadType(MonitorTypeEnum.UPLOAD_TYPE_1.getType());
        RuntimeDataContext.getData().setApiCode(apiCode);
        RuntimeDataContext.getData().setJsonData(BrCipherJsonUtils.cipherEncodeJsonDataArr(jsonData, Constants.TAG_KEY, Constants.JSON_DATA_KEYARR));
        return iPushShuheDataService.saveUploadData(apiCode, jsonData);
    }
}
