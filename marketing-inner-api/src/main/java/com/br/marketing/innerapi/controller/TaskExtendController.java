package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.service.MarketingTaskExtendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/rule/taskExtend")
@Tag(name = "跑分任务扩展", description = "跑分任务扩展")
public class TaskExtendController {


    @Autowired
    private MarketingTaskExtendService marketingTaskExtendService;

    @Operation(summary = "根据所选文件获得产品集合", description = "根据所选文件获得产品集合，支持跑分任务和上传任务")
    @Parameters({
            @Parameter(name = "ids", description = "文件ID，多个用逗号分隔", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true),
            @Parameter(name = "taskType", description = "任务类型：0-跑分任务，1-上传任务", in = ParameterIn.QUERY, schema = @Schema(type = "integer"))
    })
    @GetMapping("/getProducts")
    public ApiResult<Map<String, Set<String>>> getProducts(@RequestParam(required = true) String ids,
                                       @RequestParam(required = false) Integer taskType) {
        return new ApiResult<Map<String, Set<String>>>().success(marketingTaskExtendService.getProducts(ids, taskType));
    }


}
