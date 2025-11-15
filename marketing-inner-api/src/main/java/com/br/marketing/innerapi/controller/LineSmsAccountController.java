package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.LineAccountDto;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.entity.MarketingDict;
import com.br.marketing.service.LineSmsAccountNormalService;
import com.br.marketing.service.LineSmsAccountService;
import com.br.marketing.vo.LineAccountDetailVO;
import com.br.marketing.vo.MarketingSmsAccountRecordVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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

    @Resource
    LineSmsAccountNormalService lineSmsAccountNormalService;

    private static final Logger log = LoggerFactory.getLogger(LineSmsAccountController.class);

    private static final Integer CODE_1 = Integer.valueOf(1);

    @ApiOperation(value = "短信对账基础信息查询")
    @GetMapping("/getSmsAccountBasInfo")
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
    public ApiResult addSmsAccount(@RequestBody SmsAccountDto dto) {
        try {
            return new ApiResult().fromResult(lineSmsAccountService.addSmsAccount(dto), CODE_1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "短信对账配置变更")
    @PatchMapping("/updSmsAccount")
    public ApiResult updSmsAccount(@RequestBody SmsAccountDto dto) {
        try {
            return new ApiResult().fromResult(lineSmsAccountService.updSmsAccount(dto), CODE_1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "短信对账配置禁用")
    @PatchMapping("/forbSmsAccount")
    public ApiResult forbSmsAccount(@RequestParam Long configId) {
        try {
            return new ApiResult().fromResult(lineSmsAccountService.forbSmsAccount(configId), CODE_1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "短信对账配置启用")
    @PatchMapping("/allowSmsAccount")
    public ApiResult allowSmsAccount(@RequestParam Long configId) {
        try {
            return new ApiResult().fromResult(lineSmsAccountService.allowSmsAccount(configId), CODE_1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation(value = "短信对账配置列表查询")
    @GetMapping("/getSmsAccounts")
    public ApiResult getSmsAccounts(@RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String vendorName,
                                    @RequestParam(required = false) String channelsName,
                                    @RequestParam(required = false) Double price,
                                    @RequestParam(required = false) String configIdStr) {
        try {
            ApiResult apiResult = new ApiResult();
            if (StringUtils.isNotEmpty(configIdStr)) {
                Long configId = Long.parseLong(configIdStr);
                List<MarketingSmsAccountRecordVo> smsAccountRecordList= lineSmsAccountService.getSmsAccountsByConfigId(configId);
                apiResult = new ApiResult<List<MarketingSmsAccountRecordVo>>().success(smsAccountRecordList);
            }else{
                PageResultReturn page = lineSmsAccountService.getSmsAccounts(current,size,vendorName,channelsName,price);
                apiResult=  new ApiResult<PageResultReturn>().success(page);
            }
            return apiResult;
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "短信对账配置变更查询")
    @GetMapping("/getSmsAccountLogs")
    public ApiResult getSmsAccountLogs(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(name = "configIdStr") String configIdStr) {
        try {
            if (StringUtils.isEmpty(configIdStr)) {
                return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
            }
            Long configId = Long.parseLong(configIdStr);
            PageResultReturn page = lineSmsAccountService.getSmsAccountLogs(current,size,configId);
            return new ApiResult<>().success(page);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.AUTH_FAILED_ERROR_PARAM);
        }
    }

    @ApiOperation(value = "线路对账配置新增")
    @PostMapping("/addLineAccount")
    public ApiResult addLineAccount(@RequestBody LineAccountDto dto) {
        try {
            return new ApiResult().fromResult(lineSmsAccountNormalService.addLineAccount(dto), CODE_1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "线路对账配置变更")
    @PatchMapping("/updLineAccount")
    public ApiResult updLineAccount(@RequestBody LineAccountDto dto) {
        try {
            return new ApiResult().fromResult(lineSmsAccountService.updLineAccount(dto), CODE_1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "线路对账配置禁用")
    @PatchMapping("/forbLineAccount")
    public ApiResult forbLineAccount(@RequestParam Long groupId) {
        try {
            return new ApiResult().fromResult(lineSmsAccountNormalService.forbLineAccount(groupId), CODE_1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "线路对账配置启用")
    @PatchMapping("/allowLineAccount")
    public ApiResult allowLineAccount(@RequestParam Long groupId) {
        try {
            return new ApiResult().fromResult(lineSmsAccountNormalService.allowLineAccount(groupId), CODE_1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "线路对账基础信息查询")
    @GetMapping("/getLineAccountBasInfo")
    public ApiResult getLineAccountBasInfo() {
        try {
            return lineSmsAccountNormalService.getLineAccountBasInfo();
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "线路对账配置列表查询")
    @GetMapping("/getLineAccounts")
    public ApiResult getLineAccounts(@RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String lineSupplier,
                                    @RequestParam(required = false) String callerFullName,
                                    @RequestParam(required = false) Double price,
                                    @RequestParam(required = false) String groupIdStr) {
        try {
            ApiResult apiResult = new ApiResult();
            if (StringUtils.isNotEmpty(groupIdStr)) {
                Long groupId = Long.parseLong(groupIdStr);
                List<LineAccountDetailVO> showDtoList = lineSmsAccountNormalService.getLineAccountsByGroupId(groupId);
                apiResult = new ApiResult<List<LineAccountDetailVO>>().success(showDtoList);
            }else{
                PageResultReturn page = lineSmsAccountNormalService.getLineAccounts(current,size,lineSupplier,callerFullName,price);
                apiResult=  new ApiResult<PageResultReturn>().success(page);
            }
            return apiResult;
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }


    @ApiOperation(value = "线路对账配置变更查询")
    @GetMapping("/getLineAccountLogs")
    public ApiResult getLineAccountLogs(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(name = "sourceIdStr") String sourceIdStr) {
        try {
            if (StringUtils.isEmpty(sourceIdStr)) {
                return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
            }
            Long sourceId = Long.parseLong(sourceIdStr);
            PageResultReturn page = lineSmsAccountNormalService.getLineAccountLogs(current,size,sourceId);
            return new ApiResult<>().success(page);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.AUTH_FAILED_ERROR_PARAM);
        }
    }

    @GetMapping("/getDictInfo")
    @ApiOperation(value = "获取部门/短信类别字典列表",notes = "获取部门/短信类别字典列表")
    public ApiResult<Map<String, List<MarketingDict>>> getDictInfo(String dictType){
        try {
            Map<String, List<MarketingDict>> resultMap = lineSmsAccountService.getDictInfo(dictType);
            return new ApiResult<Map<String, List<MarketingDict>>>().success(resultMap);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return new ApiResult<Map<String, List<MarketingDict>>>().fail(ServiceResultEnum.FAILED);
        }

    }

}
