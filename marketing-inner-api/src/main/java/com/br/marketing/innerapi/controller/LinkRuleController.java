package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.service.LinkRuleService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/link/rule")
public class LinkRuleController {

    @Resource
    private LinkRuleService linkRuleService;

    @PostMapping("/createTask")
    @ApiOperation(value = "创建导出任务", notes = "短链统计导出任务创建", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskName", value = "任务名称", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "dataSource", value = "数据源编码", required = true, paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "exportHeaders", value = "导出表头", required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "fieldMapping", value = "字段映射JSON", required = true, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "queryCondition", value = "查询条件JSON", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "estimatedRows", value = "预估行数", required = true, paramType = "query", dataType = "long"),
            @ApiImplicitParam(name = "fileNameTemplate", value = "导出文件名模板", paramType = "query", dataType = "string")
    })
    public ApiResult<Boolean> createTask(@RequestParam(required = false) String taskName,
                                         @RequestParam Integer dataSource,
                                         @RequestParam String exportHeaders,
                                         @RequestParam String fieldMapping,
                                         @RequestParam(required = false) String queryCondition,
                                         @RequestParam Long estimatedRows,
                                         @RequestParam(required = false) String fileNameTemplate) {
        try {
            MarketingUserDetail user = ThreadContextInfo.getUser();
            String userName = user != null ? user.getUserName() : null;
            Boolean result = linkRuleService.createTask(taskName, dataSource, exportHeaders, fieldMapping, queryCondition, estimatedRows, fileNameTemplate, userName);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            return new ApiResult<Boolean>().fail("创建导出任务失败！");
        }
    }

}
