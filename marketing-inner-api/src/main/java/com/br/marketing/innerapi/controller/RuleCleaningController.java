package com.br.marketing.innerapi.controller;

import com.br.common.log.AlertLog;
import com.br.marketing.client.rulecleaning.FieldCleaningConfigDTO;
import com.br.marketing.client.rulecleaning.FieldCleaningPreviewDTO;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.entity.MarketingDataCleanGeneralFieldConfig;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.br.marketing.client.rulecleaning.FieldSampleDTO;
import com.br.marketing.client.rulecleaning.RuleCleaningConfigDTO;
import com.br.marketing.vo.dataclean.CleanFieldConfigVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    public ApiResult<Boolean> saveOrUpdateRule(@RequestBody RuleCleaningConfigDTO configDTO) {
        try {
            log.info("接收到保存或更新规则及清洗配置请求: {}", configDTO);
            
            // 构建规则配置对象
            MarketingDataCleanGeneralConfig config = new MarketingDataCleanGeneralConfig();
            config.setApiCode(configDTO.getApiCode());
            config.setDataType(configDTO.getDataType());
            config.setAcceptType(configDTO.getAcceptType());
            
            // 处理账号类型：以7开头的均为测试账号
            if (config.getApiCode() != null && config.getApiCode().startsWith("7")) {
                config.setAccountType("测试");
            } else {
                config.setAccountType("正式");
            }
            
            // 先保存或更新规则
            boolean ruleResult = ruleCleaningService.saveOrUpdateRule(config);
            
            // 保存字段清洗配置
            boolean cleaningResult = true;
            List<FieldCleaningConfigDTO> cleaningConfigs = configDTO.getCleaningConfig();
            if (ruleResult && cleaningConfigs != null && !cleaningConfigs.isEmpty()) {
                for (FieldCleaningConfigDTO fieldConfig : cleaningConfigs) {
                    // 设置API编码信息
                    fieldConfig.setApiCode(configDTO.getApiCode());
                    fieldConfig.setDataType(configDTO.getDataType());
                    fieldConfig.setAcceptType(configDTO.getAcceptType());
                    
                    log.info("保存字段清洗配置: {}", fieldConfig);
                    boolean singleResult = ruleCleaningService.saveFieldCleaningConfig(fieldConfig);
                    if (!singleResult) {
                        cleaningResult = false;
                        log.warn("保存字段清洗配置失败: {}", fieldConfig);
                    }
                }
            }
            
            return new ApiResult<Boolean>().success(ruleResult && cleaningResult);
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

//
//    @PostMapping("/saveFieldCleaningConfig")
//    @ApiOperation(value = "保存字段清洗配置", notes = "保存字段与清洗规则的映射关系", httpMethod = "POST")
//    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_warn")})
//    public ApiResult<Boolean> saveFieldCleaningConfig(@RequestBody FieldCleaningConfigDTO configDTO) {
//        log.info("接收到字段清洗配置请求: {}", configDTO);
//        boolean result = ruleCleaningService.saveFieldCleaningConfig(configDTO);
//        return new ApiResult<Boolean>().success(result);
//    }


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

}
