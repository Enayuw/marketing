package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.service.MarketingTaskExtendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "跑分任务扩展", description = "跑分任务扩展")
public class TaskExtendController {

    private static final Logger log = LoggerFactory.getLogger(TaskExtendController.class);

    @Autowired
    private MarketingTaskExtendService marketingTaskExtendService;

    @Operation(summary = "根据所选文件获得产品集合", description = "根据所选文件获得产品集合")
    @Parameter(name = "ids", description = "文件ID列表")
    @GetMapping("/getProducts")
    public ApiResult<Map> getProducts(@RequestParam(required = true) String ids){
            return new ApiResult<Map>().success(marketingTaskExtendService.getProducts(ids));
    }


}
