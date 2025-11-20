package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.service.MarketingTaskExtendService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/rule/taskExtend")
@Tag(value = "跑分任务扩展", tags = "跑分任务扩展", produces = "application/json", consumes = "application/json", protocols = "http")
public class TaskExtendController {


    @Autowired
    private MarketingTaskExtendService marketingTaskExtendService;

    @Operation(summary = "根据所选文件获得产品集合", description = "根据所选文件获得产品集合")
    @Parameters({
            @Parameter(name = "ids", paramType = "query", dataType = "string"),
            @Parameter(name = "taskType", value = "任务类型：0-跑分任务，1-上传任务", paramType = "query", dataType = "int")
    })
    @GetMapping("/getProducts")
    public ApiResult<Map<String, Set<String>>> getProducts(@RequestParam(required = true) String ids,
                                       @RequestParam(required = false) Integer taskType) {
        return new ApiResult<Map<String, Set<String>>>().success(marketingTaskExtendService.getProducts(ids, taskType));
    }


}
