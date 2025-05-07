package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 规则数据清洗
 * @author guangxiu.li
 * @date 2025/5/6
 */
@RestController
@RequestMapping(value = "/ruleCleaning")
@Api(value = "规则数据清洗", tags = "规则数据清洗", produces = "application/json", consumes = "application/json", protocols = "http")
@Slf4j
public class RuleCleaningController {

    @Resource
    private RuleCleaningService ruleCleaningService;

    // 规则列表查询接口
    @GetMapping("/getRuleList")
    @ApiOperation(value = "规则列表查询", notes = "规则列表查询接口", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页", paramType = "query", dataType = "integer", defaultValue = "1"),
            @ApiImplicitParam(name = "size", value = "每页条数", paramType = "query", dataType = "integer", defaultValue = "10"),
            @ApiImplicitParam(name = "apiCode", value = "API编码", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "accountType", value = "账号类型", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "acceptType", value = "接口类型", paramType = "query", dataType = "integer")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR")})
    public ApiResult<PageResultReturn> getRuleList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String apiCode,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) Integer acceptType) {
        
        PageResultReturn pageResultReturn = ruleCleaningService.getRuleList(current, size, apiCode, accountType, acceptType);
        return new ApiResult<PageResultReturn>().success(pageResultReturn);
    }

    // 规则列表新增、编辑接口
    @PostMapping("/saveOrUpdateRule")
    @ApiOperation(value = "保存或更新规则", notes = "保存或更新规则接口", httpMethod = "POST")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR")})
    public ApiResult<Boolean> saveOrUpdateRule(@RequestBody MarketingDataCleanGeneralConfig config) {
        // 处理账号类型：以7开头的均为测试账号
        if (config.getApiCode() != null && config.getApiCode().startsWith("7")) {
            config.setAccountType("测试");
        } else {
            config.setAccountType("正式");
        }
        
        boolean result = ruleCleaningService.saveOrUpdateRule(config);
        return new ApiResult<Boolean>().success(result);
    }


    // 字段样例查询接口


    // 字段清洗配置接口


    // 字段运算清洗结果预览接口


    // 字符串处理清洗结果预览接口


    // 优先级清洗结果预览接口




}
