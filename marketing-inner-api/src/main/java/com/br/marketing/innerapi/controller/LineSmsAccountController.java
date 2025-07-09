package com.br.marketing.innerapi.controller;

import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.entity.MarketingSmsAccountLog;
import com.br.marketing.entity.MarketingSmsAccountRecord;
import com.br.marketing.service.LineSmsAccountService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
        try {
            return lineSmsAccountService.getSmsAccountBasInfo();
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "短信对账配置新增")
    @PostMapping("/addSmsAccount")
    @LogAnnotation
    public ApiResult addSmsAccount(@RequestBody SmsAccountDto dto) {
        try {
            return new ApiResult().fromResult(lineSmsAccountService.addSmsAccount(dto), CODE_1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation(value = "短信对账配置变更")
    @PatchMapping("/updSmsAccount")
    @LogAnnotation
    public ApiResult updSmsAccount(@RequestBody SmsAccountDto dto) {
        try {
            return new ApiResult().fromResult(lineSmsAccountService.updSmsAccount(dto), CODE_1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation(value = "短信对账配置禁用")
    @PatchMapping("/forbSmsAccount")
    @LogAnnotation
    public ApiResult forbSmsAccount(@RequestParam Long configId) {
        try {
            return new ApiResult().fromResult(lineSmsAccountService.forbSmsAccount(configId), CODE_1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation(value = "短信对账配置列表查询")
    @GetMapping("/getSmsAccounts")
    @LogAnnotation
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "vendorName", value = "供应商名称", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "channelsName", value = "渠道名称", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "price", value = "价格", paramType = "query", dataType = "double")
            , @ApiImplicitParam(name = "configId", value = "汇总配置d", paramType = "query", dataType = "Long")
    })
    public ApiResult getSmsAccounts(@RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String vendorName,
                                    @RequestParam(required = false) String channelsName,
                                    @RequestParam(required = false) Double price,
                                    @RequestParam(required = false) Long configId) {
        try {
            ApiResult apiResult = new ApiResult();
            if (configId != null && configId>0L) {
                List<MarketingSmsAccountRecord> smsAccountRecordList= lineSmsAccountService.getSmsAccountsByConfigId(configId);
                apiResult = new ApiResult<List<MarketingSmsAccountRecord>>().success(smsAccountRecordList);
            }else{
                PageResultReturn list = lineSmsAccountService.getSmsAccounts(current,size,vendorName,channelsName,price);
                apiResult=  new ApiResult<PageResultReturn>().success(list);
            }
            return apiResult;
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "短信对账配置变更查询")
    @GetMapping("/getSmsAccountLogs")
    @LogAnnotation
    @ApiImplicitParams({
            @ApiImplicitParam(name = "configId", value = "汇总配置id", paramType = "query", dataType = "Long")
    })
    public ApiResult getSmsAccountLogs(@RequestParam(name = "configId") Long configId) {
        try {
            List<MarketingSmsAccountLog> list = lineSmsAccountService.getSmsAccountLogs(configId);
            return new ApiResult<>().success(list);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

}
