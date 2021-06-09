package com.br.marketing.api.controller;

import com.alibaba.fastjson.*;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserSyncStatusDTO;
import com.br.marketing.dto.RequestCommonDTO;
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
     * @param apiCode
     * @param jsonData
     * @return
     */
//    @ApiOperation(value = "批量接入营销人员数据")
////    @PostMapping("/receiveMarketingPreUser")
//    public ApiNoDataResult receiveMarketingPreUser(@RequestParam("apiCode")String apiCode, @RequestParam("jsonData") String jsonData){
//        try {
//            RequestCommonDTO<MarketingPreUserDTO> dto = new RequestCommonDTO<>();
//            dto.setApiCode(apiCode);
//            try {
//                dto.setJsonData(JSON.parseObject(jsonData, new TypeReference<MarketingPreUserDTO>() {
//                }.getType()));
//            }catch (JSONException ex){
//                if(ex.getMessage().contains("not match")){
//                    return new ApiNoDataResult().setCode("100006").setMessage("请核实下是否jsonData过长，jsonData解析异常");
//                }else{
//                    return new ApiNoDataResult().setCode("100006").setMessage("jsonData解析异常");
//                }
//            }
//            return new ApiNoDataResult().fromResult(pushRuleService.insertMarketingPreUser(dto));
//        }catch (ParamValidErrorException ex){
//            log.error(ex.getMessage());
//            return new ApiNoDataResult().setCode("100006").setMessage(ex.getMessage());
//        }
//    }

    /**
     * 批量接入营销人员数据
     * @param apiCode
     * @param jsonData
     * @return
     */
    @ApiOperation(value = "批量接入营销人员数据")
    @PostMapping("/receiveMarketingPreUser")
    public ApiNoDataResult receiveMarketingPreUserSync(@RequestParam("apiCode")String apiCode, @RequestParam("jsonData") String jsonData){
        try {
            long l = System.currentTimeMillis();
            RequestCommonDTO<MarketingPreUserDTO> dto = new RequestCommonDTO<>();
            dto.setApiCode(apiCode);
            try {
                dto.setJsonData(JSON.parseObject(jsonData, new TypeReference<MarketingPreUserDTO>() {
                }.getType()));
            }catch (JSONException ex){
                if(ex.getMessage().contains("not match")){
                    return new ApiNoDataResult().setCode("100006").setMessage("请核实下是否jsonData过长，jsonData解析异常");
                }else{
                    return new ApiNoDataResult().setCode("100006").setMessage("jsonData解析异常");
                }
            }
            System.out.println("第一步耗时："+(System.currentTimeMillis()-l));
            return new ApiNoDataResult().fromResult(pushRuleService.insertMarketingPreUserText(dto));
        }catch (ParamValidErrorException ex){
            log.error(ex.getMessage());
            return new ApiNoDataResult().setCode("100006").setMessage(ex.getMessage());
        }
    }

    @ApiOperation(value = "获取营销人员数据状态")
    @PostMapping("/getMarketingPreUserStauts")
    public ApiResult getMarketingPreUserStauts(@RequestParam("apiCode")String apiCode, @RequestParam("jsonData") String jsonData){
        try {
            MarketingPreUserSyncStatusDTO o = JSON.parseObject(jsonData, new TypeReference<MarketingPreUserSyncStatusDTO>() {
            }.getType());
            return new ApiResult().fromResult(pushRuleService.getMarketingPreUserSyncStatus(o));
        }catch (ParamValidErrorException ex){
            log.error(ex.getMessage());
            return new ApiResult().setCode("100006").setMessage(ex.getMessage());
        }catch (JSONException ex){

            return new ApiResult().setCode("100006").setMessage("jsonData解析异常");

        }
    }

    @GetMapping("/syncConsumer")
    public Result syncConsumer(@RequestParam("infoId") Long infoId){
        return pushRuleService.insertMarketingPreUserSync(infoId);
    }


}
