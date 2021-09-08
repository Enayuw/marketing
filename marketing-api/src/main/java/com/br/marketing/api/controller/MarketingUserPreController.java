package com.br.marketing.api.controller;

import com.alibaba.fastjson.*;
import com.br.marketing.common.annoation.SaveLog;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.exception.CommonException;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.dto.MarketingPreUserSyncStatusDTO;
import com.br.marketing.service.PushRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;

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


    /**
     * 批量接入营销人员数据
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @ApiOperation(value = "批量接入营销人员数据")
    @PostMapping("/receiveMarketingPreUser")
    @Consumes(value = "application/x-www-form-urlencoded")
    public ApiNoDataResult receiveMarketingPreUserSync(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        try {
            long l = System.currentTimeMillis();
            Result result = pushRuleService.insertMarketingPreUserText(apiCode, jsonData);
            if (log.isInfoEnabled()) {
                log.info("接入营销人员接口耗时：{}", (System.currentTimeMillis() - l));
            }
            return new ApiNoDataResult().fromResult(result);
        } catch (CommonException ex) {
            log.error(ex.getMessage());
            MarketingErrorInfo info = ex.getInfo();
            return new ApiNoDataResult().setCode(info.getErrorCode()).setMessage(info.getErrorMsg());
        }
    }

    @ApiOperation(value = "转化人员")
    @PostMapping("/transferUser")
    @SaveLog
    public ApiNoDataResult transferUser(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData){
        try {
            long l = System.currentTimeMillis();
            Result result = pushRuleService.insertBatchTransferUser(apiCode, jsonData);
            return new ApiNoDataResult().fromResult(result);
        } catch (CommonException ex) {
            log.error(ex.getMessage());
            MarketingErrorInfo info = ex.getInfo();
            return new ApiNoDataResult().setCode(info.getErrorCode()).setMessage(info.getErrorMsg());
        }
    }

    /**
     * 获取营销人员数据状态
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @ApiOperation(value = "获取营销人员数据状态")
    @PostMapping("/getMarketingPreUserStauts")
    public ApiResult getMarketingPreUserStauts(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        try {
            MarketingPreUserSyncStatusDTO o = JSON.parseObject(jsonData,
                    new TypeReference<MarketingPreUserSyncStatusDTO>() {
                    }.getType());
            o.setApiCode(apiCode);
            return new ApiResult().fromResult(pushRuleService.getMarketingPreUserSyncStatus(o));
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
     * @param custNum
     * @return
     */
    @ApiOperation(value = "查询客户信息接口")
    @PostMapping("/queryCustInfo")
    public Result queryCustInfo(@RequestParam("cid") String cid, @RequestParam("custNum") String custNum) {
        try {
            return pushRuleService.queryCustInfo(cid, custNum);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
    }
}
