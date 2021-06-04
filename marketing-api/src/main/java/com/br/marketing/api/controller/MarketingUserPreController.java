package com.br.marketing.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.RequestCommonDTO;
import com.br.marketing.service.PushRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/marketingUserPre")
@RestController
public class MarketingUserPreController {

    @Autowired
    PushRuleService pushRuleService;

    @PostMapping("/receiveMarketingPreUser")
    public ApiNoDataResult receiveMarketingPreUser(@RequestParam("apiCode")String apiCode, @RequestParam("jsonData") String jsonData){
        try {
            RequestCommonDTO<MarketingPreUserDTO> dto = new RequestCommonDTO<>();
            dto.setApiCode(apiCode);
            dto.setJsonData(JSON.parseObject(jsonData,new TypeReference<MarketingPreUserDTO>(){}.getType()));
            return new ApiNoDataResult().fromResult(pushRuleService.insertMarketingPreUser(dto));
        }catch (ParamValidErrorException ex){
            return new ApiNoDataResult().setCode("100006").setMessage(ex.getMessage());
        }
    }
}
