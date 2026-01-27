package com.br.marketing.innerapi.controller;

import com.br.marketing.client.rulecleaning.*;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.entity.MarketingDataCleanGeneralFieldConfig;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.br.marketing.vo.dataclean.CleanFieldConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import com.br.marketing.client.rulecleaning.CleanConfigDTO;


/**
 * 规则数据清洗
 *
 * @author guangxiu.li
 * @date 2025/5/6
 */
@RestController
@RequestMapping(value = "/ruleCleaning")
@Tag(name = "规则数据清洗", description = "规则数据清洗")
@Slf4j
public class RuleCleaningController {

    @Resource
    private RuleCleaningService ruleCleaningService;

    @GetMapping("/getRuleList")
    @Operation(summary = "规则列表查询", description = "规则列表查询接口")
    @Parameters({
            @Parameter(name = "current", description = "当前页"),
            @Parameter(name = "size", description = "每页条数"),
            @Parameter(name = "apiCode", description = "API编码"),
            @Parameter(name = "accountType", description = "账号类型"),
            @Parameter(name = "acceptType", description = "接口类型")
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_warn")})
    public ApiResult<PageResultReturn> getRuleList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String apiCode,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) Integer acceptType) {
        
        PageResultReturn pageResultReturn = ruleCleaningService.getRuleList(current, size, apiCode, accountType, acceptType);
        return new ApiResult<PageResultReturn>().success(pageResultReturn);
    }

    @GetMapping("/getRuleDetailById")
    @Operation(summary = "根据规则ID查询规则明细", description = "根据规则ID查询规则明细")
    @Parameters({
            @Parameter(name = "ruleId", description = "规则ID", required = true)
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_warn")})
    public ApiResult<MarketingDataCleanGeneralConfig> getRuleDetailById(@RequestParam("ruleId") Long ruleId) {
        MarketingDataCleanGeneralConfig ruleDetail = ruleCleaningService.getRuleDetailById(ruleId);
        if (ruleDetail == null) {
            throw new BusinessException("规则不存在");
        }
        return new ApiResult<MarketingDataCleanGeneralConfig>().success(ruleDetail);
    }

    @GetMapping("/getPreviewFieldSamples")
    @Operation(summary = "新增配置字段样例查询", description = "新增配置字段样例查询")
    @Parameters({
            @Parameter(name = "apiCode", description = "API编码", required = true),
            @Parameter(name = "systemType", description = "数据来源,0-营销中台 1-外呼系统", required = true),
            @Parameter(name = "dataType", description = "数据类型：0上传，1转化", required = true),
            @Parameter(name = "acceptType", description = "接口类型：0通用,1定制,2FTP", required = true)
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_warn")})
    public ApiResult<List<FieldSampleDTO>> getPreviewFieldSamples(
            @RequestParam String apiCode,
            @RequestParam Integer systemType,
            @RequestParam Integer dataType,
            @RequestParam Integer acceptType) {

        List<FieldSampleDTO> fieldSamples = ruleCleaningService.getPreviewFieldSamples(apiCode, systemType, dataType, acceptType);
        return new ApiResult<List<FieldSampleDTO>>().success(fieldSamples);
    }



    @GetMapping("/getFieldSamples")
    @Operation(summary = "字段样例查询", description = "查询定制化接口字段和字段样例")
    @Parameters({
            @Parameter(name = "apiCode", description = "API编码", required = true),
            @Parameter(name = "systemType", description = "数据来源,0-营销中台 1-外呼系统", required = true),
            @Parameter(name = "dataType", description = "数据类型：0上传，1转化", required = true),
            @Parameter(name = "acceptType", description = "接口类型：0通用,1定制,2FTP", required = true)
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_warn")})
    public ApiResult<List<FieldSampleDTO>> getFieldSamples(
            @RequestParam String apiCode,
            @RequestParam Integer systemType,
            @RequestParam Integer dataType,
            @RequestParam Integer acceptType) {
        
        List<FieldSampleDTO> fieldSamples = ruleCleaningService.getFieldSamples(apiCode, systemType, dataType, acceptType);
        return new ApiResult<List<FieldSampleDTO>>().success(fieldSamples);
    }


    @GetMapping("/getpreviewField")
    @Operation(summary = "数据预览", description = "数据预览")
    @Parameters({
            @Parameter(name = "apiCode", description = "API编码", required = true),
            @Parameter(name = "systemType", description = "数据来源,0-营销中台 1-外呼系统", required = true),
            @Parameter(name = "dataType", description = "数据类型：0上传，1转化", required = true),
            @Parameter(name = "acceptType", description = "接口类型：0通用,1定制,2FTP", required = true)
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_warn")})
    public ApiResult<String> getpreviewField(
            @RequestParam String apiCode,
            @RequestParam Integer systemType,
            @RequestParam Integer dataType,
            @RequestParam Integer acceptType) {
        
        String fieldSamples = ruleCleaningService.getpreviewField(apiCode,systemType, dataType, acceptType);
        return new ApiResult<String>().success().setData(fieldSamples);
    }



    @PostMapping("/previewFieldCleaning")
    @Operation(summary = "字段清洗结果预览", description = "预览字段清洗规则应用后的结果")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_warn")})
    public ApiResult<Object> previewFieldCleaning(
            @RequestBody @Parameter(description = "预览请求参数", required = true) FieldCleaningPreviewDTO previewDTO) {
        
        log.info("接收到字段清洗预览请求: {}", previewDTO);
        Object cleanedData = ruleCleaningService.previewFieldCleaning(previewDTO.getFieldSample(),
                previewDTO.getCleaningRule(), null);
        return new ApiResult<Object>().success(cleanedData);
    }


    @PostMapping("/field/saveOrUpdate")
    @Operation(summary = "模版字段配置保存更新", description = "模版字段配置保存更新")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_warn")})
    public ApiResult<Boolean> fieldSaveOrUpdate(@RequestBody CleanFieldConfigVO fieldConfigVO) {
        boolean result = ruleCleaningService.fieldSaveOrUpdate(fieldConfigVO);
        return new ApiResult<Boolean>().success(result);
    }


    @GetMapping("/field/getFieldConfig")
    @Operation(summary = "模版字段配置查询", description = "模版字段配置查询")
    @Parameters({
            @Parameter(name = "dataType", description = "数据类型：0上传，1转化", required = true),
            @Parameter(name = "acceptType", description = "接口类型：0通用,1定制,2FTP"),
            @Parameter(name = "systemType", description = "数据来源,0-营销中台 1-外呼系统", required = true),
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_warn")})
    public ApiResult<MarketingDataCleanGeneralFieldConfig> getFieldConfg(@RequestParam(required = true) Integer dataType,
                                                                         @RequestParam(required = false) Integer acceptType,
                                                                         @RequestParam(required = true) Integer systemType) {

        MarketingDataCleanGeneralFieldConfig fieldConfg = ruleCleaningService.getFieldConfg(dataType, acceptType, systemType);
        return new ApiResult<MarketingDataCleanGeneralFieldConfig>().success(fieldConfg);
    }

    @GetMapping("/getLastMonthDataDates")
    @Operation(summary = "查询近一个月有数据的日期集合", description = "查询近一个月有数据的日期集合")
    @Parameters({
            @Parameter(name = "apiCode", description = "API编码", required = true),
            @Parameter(name = "acceptType", description = "接口类型：0通用,1定制,2FTP", required = true),
            @Parameter(name = "sftpPath", description = "sftp地址，接口类型为FTP则必填")
    })
    public ApiResult<List<String>> getLastMonthDataDates(@RequestParam("apiCode") String apiCode, @RequestParam("acceptType") Integer acceptType, @RequestParam(required = false) String sftpPath) {

        List<String> result = ruleCleaningService.getLastMonthDataDates(apiCode, acceptType, sftpPath);
        return new ApiResult<List<String>>().success(result);
    }


    @PostMapping("/config/save")
    @Operation(summary = "清洗配置保存", description = "清洗配置保存")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_warn")})
    public ApiResult<Boolean> saveConfig(@RequestBody @Validated CleanConfigDTO configDTO) {
        // 调用Service处理业务逻辑
        boolean result = ruleCleaningService.saveCleanConfig(configDTO);
        return new ApiResult<Boolean>().success(result);
    }


    @GetMapping("/getFileSftpPath")
    @Operation(summary = "获取文件SFTP路径", description = "获取文件SFTP路径")
    @Parameters({
            @Parameter(name = "apiCode", description = "API编码", required = true),
            @Parameter(name = "fileType", description = "文件类型：13:上传清洗周期文件,14:转化清洗周期文件", required = true)

    })
    public ApiResult<List<String>> getFileSftpPath(@RequestParam("apiCode") String apiCode,@RequestParam("fileType") Integer fileType) {
        List<String> dates = ruleCleaningService.getFileSftpPath(apiCode, fileType);
        return new ApiResult<List<String>>().success(dates);
    }

    @GetMapping("/getRuleDetail")
    @Operation(summary = "获取清洗规则配置", description = "获取清洗规则配置")
    @Parameters({
            @Parameter(name = "configId", description = "配置Id", required = true)

    })
    public ApiResult<List<FieldSampleDTO>> getRuleDetail(@RequestParam("configId") Long configId) {
        List<FieldSampleDTO> ruleDetails = ruleCleaningService.getRuleDetail(configId);
        return new ApiResult<List<FieldSampleDTO>>().success(ruleDetails);
    }


    @PostMapping("/rule/save")
    @Operation(summary = "清洗规则保存", description = "清洗规则保存")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_warn")})
    public ApiResult<Boolean> saveCleanRule(@RequestBody @Validated RuleCleaningConfigDTO ruleCleaningConfigDTO) {
        // 调用Service处理业务逻辑
        boolean result = ruleCleaningService.saveCleanRule(ruleCleaningConfigDTO);
        return new ApiResult<Boolean>().success(result);
    }


    @PostMapping("/trailProcess")
    @Operation(summary = "试跑验证规则有效性", description = "试跑验证规则有效性")
    public ApiResult<List<List<RuleCleaningResult>>> trailProcess(@RequestBody RuleTrialConfigDTO ruleTrialConfigDTO){
        Result<List<List<RuleCleaningResult>>> result = ruleCleaningService.trialProcess(ruleTrialConfigDTO);
        return new ApiResult<List<List<RuleCleaningResult>>>().setData(result.getData()).success(result.getMessage());
    }

    @PostMapping("/ruleEffect")
    @Operation(summary = "规则生效处理", description = "规则生效处理")
    public ApiResult<Boolean> ruleEffect(@RequestParam("ruleId") Long ruleId){
        boolean result = ruleCleaningService.ruleEffect(ruleId);
        return new ApiResult<Boolean>().success(result);
    }

    @GetMapping("/generateAviatorScriptRule")
    @Operation(summary = "生成Aviator脚本规则", description = "生成Aviator脚本规则")
    @Parameters({
            @Parameter(name = "question", description = "问题内容", required = true)
    })
    public ApiResult<String> generateAviatorScriptRule(@RequestParam("question") String question) {
        String result = ruleCleaningService.generateAviatorScriptRule(question);
        return new ApiResult<String>().success().setData(result);
    }

}
