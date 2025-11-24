package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.service.MarketingTaskExtendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/rule/taskExtend")
@Api(value = "跑分任务扩展", tags = "跑分任务扩展", produces = "application/json", consumes = "application/json", protocols = "http")
public class TaskExtendController {


    @Autowired
    private MarketingTaskExtendService marketingTaskExtendService;

    @ApiOperation(value = "根据所选文件获得产品集合", notes = "根据所选文件获得产品集合，支持跑分任务和上传任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "文件ID，多个用逗号分隔", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "taskType", value = "任务类型：0-跑分任务，1-上传任务", paramType = "query", dataType = "int")
    })
    @GetMapping("/getProducts")
    public ApiResult<Map<String, Set<String>>> getProducts(@RequestParam(required = true) String ids,
                                       @RequestParam(required = false) Integer taskType) {
        return new ApiResult<Map<String, Set<String>>>().success(marketingTaskExtendService.getProducts(ids, taskType));
    }


}
