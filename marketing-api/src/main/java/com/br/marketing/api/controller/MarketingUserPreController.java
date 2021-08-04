package com.br.marketing.api.controller;

import com.alibaba.fastjson.*;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.dto.MarketingPreUserSyncStatusDTO;
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
    public ApiNoDataResult receiveMarketingPreUserSync(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        try {
            long l = System.currentTimeMillis();
            Result result = pushRuleService.insertMarketingPreUserText(apiCode, jsonData);
            if (log.isInfoEnabled()) {
                log.info("接入营销人员接口耗时：{}", (System.currentTimeMillis() - l));
            }
            return new ApiNoDataResult().fromResult(result);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage());
            return new ApiNoDataResult().setCode("100006").setMessage(ex.getMessage());
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
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage());
            return new ApiResult().setCode("100006").setMessage(ex.getMessage());
        } catch (JSONException ex) {

            return new ApiResult().setCode("100006").setMessage("jsonData解析异常");

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
