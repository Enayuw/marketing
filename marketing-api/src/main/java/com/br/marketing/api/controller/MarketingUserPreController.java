package com.br.marketing.api.controller;

import com.alibaba.fastjson.*;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.dto.MarketingPreUserDTO;
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
    @ApiOperation(value = "批量接入营销人员数据")
    @PostMapping("/receiveMarketingPreUser")
    public ApiNoDataResult receiveMarketingPreUser(@RequestParam("apiCode")String apiCode, @RequestParam("jsonData") String jsonData){
        try {
            RequestCommonDTO<MarketingPreUserDTO> dto = new RequestCommonDTO<>();
            dto.setApiCode(apiCode);
            dto.setJsonData(JSON.parseObject(jsonData,new TypeReference<MarketingPreUserDTO>(){}.getType()));
            return new ApiNoDataResult().fromResult(pushRuleService.insertMarketingPreUser(dto));
        }catch (ParamValidErrorException ex){
            log.error(ex.getMessage());
            return new ApiNoDataResult().setCode("100006").setMessage(ex.getMessage());
        }
    }
}
