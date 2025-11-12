package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.service.MarketingTaskExtendService;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rule/taskExtend")
@Tag(value = "跑分任务扩展", tags = "跑分任务扩展", produces = "application/json", consumes = "application/json", protocols = "http")
public class TaskExtendController {

    private static final Logger log = LoggerFactory.getLogger(TaskExtendController.class);

    @Autowired
    private MarketingTaskExtendService marketingTaskExtendService;

    @Operation(summary = "根据所选文件获得产品集合", description = "根据所选文件获得产品集合")
    @Parameter(name = "ids", paramType = "query", dataType = "string")
    @GetMapping("/getProducts")
    public ApiResult<Map> getProducts(@RequestParam(required = true) String ids){
            return new ApiResult<Map>().success(marketingTaskExtendService.getProducts(ids));
    }


}
