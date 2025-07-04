package com.br.marketing.innerapi.controller;

import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.account.SmsAccountDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 线路&短信对账配置管理
 * 技术方案：https://c.100credit.cn/pages/viewpage.action?pageId=212864694
 * dongshuo.he
 */
@RestController
@RequestMapping("/account")
@Api(value = "LineSmsAccountController")
public class LineSmsAccountController {

    private static final Logger log = LoggerFactory.getLogger(LineSmsAccountController.class);

    @ApiOperation(value = "短信对账基础信息查询")
    @GetMapping("/getSmsAccountBasInfo")
    @LogAnnotation
    public ApiResult getSmsAccountBasInfo() {
        return null;
    }

    @ApiOperation(value = "短信对账配置新增")
    @PostMapping("/addSmsAccount")
    @LogAnnotation
    public ApiResult addSmsAccount(SmsAccountDto dto) {
        return null;
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
    public ApiResult getSmsAccounts() {
        return null;
    }

    @ApiOperation(value = "短信对账配置列表查询")
    @GetMapping("/getSmsAccountLogs")
    @LogAnnotation
    public ApiResult getSmsAccountLogs() {
        return null;
    }


}
