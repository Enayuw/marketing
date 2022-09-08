package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.OffLineCallBackDTO;
import com.br.marketing.service.MarketingTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/offLineScore")
public class OffLineScoreController {

    @Autowired
    MarketingTaskService taskService;

    @PostMapping("/callBackOffLineReq")
    public ApiResult callBackOffLineReq(@RequestBody OffLineCallBackDTO dto){
        return new ApiResult().fromResult(taskService.offLineCallBack(dto),1);
    }
}
