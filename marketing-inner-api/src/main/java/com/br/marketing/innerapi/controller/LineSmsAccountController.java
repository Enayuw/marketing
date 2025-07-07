package com.br.marketing.innerapi.controller;

import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.service.LineSmsAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 线路&短信对账配置管理
 * 技术方案：https://c.100credit.cn/pages/viewpage.action?pageId=212864694
 * dongshuo.he
 */
@RestController
@RequestMapping("/account")
@Api(value = "LineSmsAccountController")
public class LineSmsAccountController {

    @Resource
    LineSmsAccountService lineSmsAccountService;

    private static final Logger log = LoggerFactory.getLogger(LineSmsAccountController.class);

    private static final Integer CODE_1 = Integer.valueOf(1);

    @ApiOperation(value = "短信对账基础信息查询")
    @GetMapping("/getSmsAccountBasInfo")
    @LogAnnotation
    public ApiResult getSmsAccountBasInfo() {
        return lineSmsAccountService.getSmsAccountBasInfo();
    }

    @ApiOperation(value = "短信对账配置新增")
    @PostMapping("/addSmsAccount")
    @LogAnnotation
    public ApiResult<Boolean> addSmsAccount(SmsAccountDto dto) {
        return new ApiResult<Boolean>().fromResult(lineSmsAccountService.addSmsAccount(dto), CODE_1);
    }

    @ApiOperation(value = "短信对账配置变更")
    @PatchMapping("/updSmsAccount")
    @LogAnnotation
    public ApiResult updSmsAccount(SmsAccountDto dto) {
        return null;
    }

    @ApiOperation(value = "短信对账配置列表查询")
    @GetMapping("/getSmsAccounts")
    @LogAnnotation
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "vendorName", value = "供应商名称", paramType = "query", dataType = "string")
    })
    public ApiResult getSmsAccounts(@RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String vendorName) {
        PageResultReturn list = lineSmsAccountService.getSmsAccounts(current,size,vendorName);
        return new ApiResult<PageResultReturn>().success(list);
    }

    @ApiOperation(value = "短信对账配置变更查询")
    @GetMapping("/getSmsAccountLogs")
    @LogAnnotation
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "recordId", value = "汇总记录id", paramType = "query", dataType = "Long")
            , @ApiImplicitParam(name = "vendorName", value = "供应商名称", paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "vendorName", value = "供应商名称", paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "optUserName", value = "操作人", paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "optType", value = "操作类型 1-新增 2-变更 3-删除", paramType = "query", dataType = "int")
    })
    public ApiResult getSmsAccountLogs(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) Long recordId,
                                       @RequestParam(required = false) String vendorName,
                                       @RequestParam(required = false) String optUserName,
                                       @RequestParam(required = false) Integer optType) {
        PageResultReturn list = lineSmsAccountService.getSmsAccountLogs(current,size,recordId,vendorName,optUserName,optType);
        return new ApiResult<PageResultReturn>().success(list);
    }


}
