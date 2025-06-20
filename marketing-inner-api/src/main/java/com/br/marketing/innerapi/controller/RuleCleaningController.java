package com.br.marketing.innerapi.controller;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.rulecleaning.*;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.entity.MarketingDataCleanGeneralFieldConfig;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.br.marketing.vo.dataclean.CleanFieldConfigVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.br.marketing.client.rulecleaning.CleanConfigDTO;


/**
 * 规则数据清洗
 *
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

    @Resource
    private RedisChgService redisChgService;

    @GetMapping("/getRuleList")
    @ApiOperation(value = "规则列表查询", notes = "规则列表查询接口", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页", paramType = "query", dataType = "integer", defaultValue = "1"),
            @ApiImplicitParam(name = "size", value = "每页条数", paramType = "query", dataType = "integer", defaultValue = "10"),
            @ApiImplicitParam(name = "apiCode", value = "API编码", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "accountType", value = "账号类型", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "acceptType", value = "接口类型", paramType = "query", dataType = "integer")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_warn")})
    public ApiResult<PageResultReturn> getRuleList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String apiCode,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) Integer acceptType) {
        
        try {
            PageResultReturn pageResultReturn = ruleCleaningService.getRuleList(current, size, apiCode, accountType, acceptType);
            return new ApiResult<PageResultReturn>().success(pageResultReturn);
        } catch (BusinessException be) {
            return new ApiResult<PageResultReturn>().fail(be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "获取规则列表接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/saveOrUpdateRule")
    @ApiOperation(value = "保存或更新规则及清洗配置", notes = "先保存或更新规则，然后保存清洗配置", httpMethod = "POST")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_warn")})
    public ApiResult<Boolean> saveOrUpdateRule(@RequestBody @Validated RuleCleaningConfigDTO configDTO) {
        try {
            log.info("接收到保存或更新规则及清洗配置请求: {}", configDTO);
            
            // 调用Service处理业务逻辑
            boolean result = ruleCleaningService.saveRuleWithConfigs(configDTO);
            
            return new ApiResult<Boolean>().success(result);
        } catch (BusinessException be) {
            return new ApiResult<Boolean>().fail(false, be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "保存或更新规则接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getPreviewFieldSamples")
    @ApiOperation(value = "新增配置字段样例查询", notes = "新增配置字段样例查询", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "apiCode", value = "API编码", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "dataType", value = "数据类型：0上传，1转化", paramType = "query", dataType = "integer", required = true),
            @ApiImplicitParam(name = "acceptType", value = "接口类型：0通用,1定制,2FTP", paramType = "query", dataType = "integer", required = true)
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_warn")})
    public ApiResult<List<FieldSampleDTO>> getPreviewFieldSamples(
            @RequestParam String apiCode,
            @RequestParam Integer dataType,
            @RequestParam Integer acceptType) {

        try {
            List<FieldSampleDTO> fieldSamples = ruleCleaningService.getPreviewFieldSamples(apiCode, dataType, acceptType);
            return new ApiResult<List<FieldSampleDTO>>().success(fieldSamples);
        } catch (BusinessException be) {
            return new ApiResult<List<FieldSampleDTO>>().fail(be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "获取字段样例接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<List<FieldSampleDTO>>().fail(ServiceResultEnum.FAILED);
        }
    }



    @GetMapping("/getFieldSamples")
    @ApiOperation(value = "字段样例查询", notes = "查询定制化接口字段和字段样例", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "apiCode", value = "API编码", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "dataType", value = "数据类型：0上传，1转化", paramType = "query", dataType = "integer", required = true),
            @ApiImplicitParam(name = "acceptType", value = "接口类型：0通用,1定制,2FTP", paramType = "query", dataType = "integer", required = true)
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_warn")})
    public ApiResult<List<FieldSampleDTO>> getFieldSamples(
            @RequestParam String apiCode,
            @RequestParam Integer dataType,
            @RequestParam Integer acceptType) {
        
        try {
            List<FieldSampleDTO> fieldSamples = ruleCleaningService.getFieldSamples(apiCode, dataType, acceptType);
            return new ApiResult<List<FieldSampleDTO>>().success(fieldSamples);
        } catch (BusinessException be) {
            return new ApiResult<List<FieldSampleDTO>>().fail(be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "获取字段样例接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<List<FieldSampleDTO>>().fail(ServiceResultEnum.FAILED);
        }
    }


    @GetMapping("/getpreviewField")
    @ApiOperation(value = "数据预览", notes = "数据预览", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "apiCode", value = "API编码", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "dataType", value = "数据类型：0上传，1转化", paramType = "query", dataType = "integer", required = true),
            @ApiImplicitParam(name = "acceptType", value = "接口类型：0通用,1定制,2FTP", paramType = "query", dataType = "integer", required = true)
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_warn")})
    public ApiResult<String> getpreviewField(
            @RequestParam String apiCode,
            @RequestParam Integer dataType,
            @RequestParam Integer acceptType) {
        
        try {
            String fieldSamples = ruleCleaningService.getpreviewField(apiCode, dataType, acceptType);
            return new ApiResult<String>().success().setData(fieldSamples);
        } catch (BusinessException be) {
            return new ApiResult<String>().fail(be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "获取数据预览接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<String>().fail(ServiceResultEnum.FAILED);
        }
    }



    @PostMapping("/previewFieldCleaning")
    @ApiOperation(value = "字段清洗结果预览", notes = "预览字段清洗规则应用后的结果", httpMethod = "POST")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_warn")})
    public ApiResult<Object> previewFieldCleaning(
            @RequestBody @ApiParam(value = "预览请求参数", required = true) FieldCleaningPreviewDTO previewDTO) {
        
        try {
            log.info("接收到字段清洗预览请求: {}", previewDTO);
            Object cleanedData = ruleCleaningService.previewFieldCleaning(previewDTO.getFieldSample(), previewDTO.getCleaningRule());
            return new ApiResult<Object>().success(cleanedData);
        } catch (BusinessException be) {
            return new ApiResult<Object>().fail(be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "预览字段清洗结果接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<Object>().fail(ServiceResultEnum.FAILED);
        }
    }


    @PostMapping("/field/saveOrUpdate")
    @ApiOperation(value = "模版字段配置保存更新", notes = "模版字段配置保存更新", httpMethod = "POST")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_warn")})
    public ApiResult<Boolean> fieldSaveOrUpdate(@RequestBody CleanFieldConfigVO fieldConfigVO) {
        try {
            boolean result = ruleCleaningService.fieldSaveOrUpdate(fieldConfigVO);
            return new ApiResult<Boolean>().success(result);
        } catch (BusinessException be) {
            return new ApiResult<Boolean>().fail(false, be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "模版字段配置保存更新接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }


    @GetMapping("/field/getFieldConfig")
    @ApiOperation(value = "模版字段配置查询", notes = "模版字段配置查询", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dataType", value = "数据类型：0上传，1转化", paramType = "query", dataType = "integer", required = true),
            @ApiImplicitParam(name = "acceptType", value = "接口类型：0通用,1定制,2FTP", paramType = "query", dataType = "integer", required = false)
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_warn")})
    public ApiResult<MarketingDataCleanGeneralFieldConfig> getFieldConfg(@RequestParam(required = true) Integer dataType,
                                                                         @RequestParam(required = false) Integer acceptType) {
        
        try {
            MarketingDataCleanGeneralFieldConfig fieldConfg = ruleCleaningService.getFieldConfg(dataType, acceptType);
            return new ApiResult<MarketingDataCleanGeneralFieldConfig>().success(fieldConfg);
        } catch (BusinessException be) {
            return new ApiResult<MarketingDataCleanGeneralFieldConfig>().fail(be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "获取模版字段配置接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<MarketingDataCleanGeneralFieldConfig>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getLastMonthDataDates")
    @ApiOperation(value = "查询近一个月有数据的日期集合", notes = "查询近一个月有数据的日期集合", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "apiCode", value = "API编码", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "acceptType", value = "接口类型：0通用,1定制,2FTP", paramType = "query", dataType = "integer", required = true),
            @ApiImplicitParam(name = "sftpPath", value = "sftp地址，接口类型为FTP则必填", paramType = "query", dataType = "String", required = false)
    })
    public ApiResult<List<String>> getLastMonthDataDates(@RequestParam("apiCode") String apiCode, @RequestParam("acceptType") Integer acceptType, @RequestParam(required = false) String sftpPath) {

        try {
            List<String> result = ruleCleaningService.getLastMonthDataDates(apiCode, acceptType, sftpPath);
            return new ApiResult<List<String>>().success(result);
        }catch (BusinessException be){
            return new ApiResult<List<String>>().fail(be.getMsg());
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.LASTMONTHDATDDATES_SERVICEERROR.getCode(),
                    "获取近一个月有数据的日期失败！错误信息：" + e.getMessage()),e);
            return new ApiResult<List<String>>().fail(ServiceResultEnum.FAILED);
        }
    }


    @PostMapping("/config/save")
    @ApiOperation(value = "清洗配置保存", notes = "清洗配置保存", httpMethod = "POST")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_warn")})
    public ApiResult<Boolean> saveConfig(@RequestBody @Validated CleanConfigDTO configDTO) {
        try {
            // 调用Service处理业务逻辑
            boolean result = ruleCleaningService.saveCleanConfig(configDTO);
            return new ApiResult<Boolean>().success(result);
        } catch (BusinessException be) {
            return new ApiResult<Boolean>().fail(false, be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "规则配置保存接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }


    @GetMapping("/getFileSftpPath")
    @ApiOperation(value = "获取文件SFTP路径", notes = "获取文件SFTP路径", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "apiCode", value = "API编码", paramType = "query", dataType = "string", required = true),
            @ApiImplicitParam(name = "fileType", value = "文件类型：13:上传清洗周期文件,14:转化清洗周期文件", paramType = "query", dataType = "integer", required = true)

    })
    public ApiResult<List<String>> getFileSftpPath(@RequestParam("apiCode") String apiCode,@RequestParam("fileType") Integer fileType) {
        try {
            List<String> dates = ruleCleaningService.getFileSftpPath(apiCode, fileType);
            return new ApiResult<List<String>>().success(dates);
        } catch (BusinessException be) {
            return new ApiResult<List<String>>().fail(be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "获取文件SFTP路径失败！错误信息：" + e.getMessage()), e);
            return new ApiResult<List<String>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getRuleDetail")
    @ApiOperation(value = "获取清洗规则配置", notes = "获取清洗规则配置", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "configId", value = "配置Id", paramType = "query", dataType = "Long", required = true)

    })
    public ApiResult<List<FieldSampleDTO>> getRuleDetail(@RequestParam("configId") Long configId) {
        try {
            List<FieldSampleDTO> ruleDetails = ruleCleaningService.getRuleDetail(configId);
            return new ApiResult<List<FieldSampleDTO>>().success(ruleDetails);
        } catch (BusinessException be) {
            return new ApiResult<List<FieldSampleDTO>>().fail(be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "获取清洗规则配置！错误信息：" + e.getMessage()), e);
            return new ApiResult<List<FieldSampleDTO>>().fail(ServiceResultEnum.FAILED);
        }
    }


    @PostMapping("/rule/save")
    @ApiOperation(value = "清洗规则保存", notes = "清洗规则保存", httpMethod = "POST")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_warn")})
    public ApiResult<Boolean> saveCleanRule(@RequestBody @Validated RuleCleaningConfigDTO ruleCleaningConfigDTO) {
        try {
            // 调用Service处理业务逻辑
            boolean result = ruleCleaningService.saveCleanRule(ruleCleaningConfigDTO);
            return new ApiResult<Boolean>().success(result);
        } catch (BusinessException be) {
            return new ApiResult<Boolean>().fail(false, be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "清洗规则保存接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }


    @PostMapping("/trailProcess")
    @ApiOperation(value = "试跑验证规则有效性",notes = "试跑验证规则有效性",httpMethod = "POST")
    public ApiResult<List<List<RuleCleaningResult>>> trailProcess(@RequestBody RuleTrialConfigDTO ruleTrialConfigDTO){
        try {
            Result<List<List<RuleCleaningResult>>> result = ruleCleaningService.trialProcess(ruleTrialConfigDTO);
            return new ApiResult<List<List<RuleCleaningResult>>>().setData(result.getData()).success(result.getMessage());
        }catch (BusinessException be) {
            return new ApiResult<List<List<RuleCleaningResult>>>().fail("",be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_TRIALPROCESSERROR.getCode(),
                    "规则试跑接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<List<List<RuleCleaningResult>>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/ruleEffect")
    @ApiOperation(value = "规则生效处理",notes = "规则生效处理",httpMethod = "POST")
    public ApiResult<Boolean> ruleEffect(@RequestParam("ruleId") Long ruleId){
        try {
            boolean result = ruleCleaningService.ruleEffect(ruleId);
            return new ApiResult<Boolean>().success(result);
        }catch (BusinessException be) {
            return new ApiResult<Boolean>().fail(false,be.getMsg());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DATACLEANING_SERVICEERROR.getCode(),
                    "规则生效处理接口错误！错误信息：" + e.getMessage()), e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

}
