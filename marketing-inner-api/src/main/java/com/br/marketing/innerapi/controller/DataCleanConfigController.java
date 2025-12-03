package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.dataclean.DataCleanConfigDTO;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.MarketingDataFileConfig;
import com.br.marketing.innerapi.service.dataclean.DataCleanHandlerService;
import com.br.marketing.vo.dataclean.DataCleanConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据清洗配置页面Controller
 * <p>
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @Description: 数据清洗配置页面
 * @CreateTime: 2024-05-23 19 :28
 * @Version: 1.0
 * @Author: zhen.Li1
 * ------------------------------
 */
@RestController
@RequestMapping(value = "/dataclean/config")
@Tag(name = "数据清洗配置页面", description = "数据清洗配置页面")
public class DataCleanConfigController {

    /**
     * CODE_1
     */
    private static final Integer CODE_1 = Integer.valueOf(1);
    @Autowired
    private DataCleanHandlerService dataCleanHandlerService;


    @GetMapping("/list")
    @Operation(summary = "清洗配置列表", description = "清洗配置列表")
    @Parameters({@Parameter(name = "current", description = "页号")
            , @Parameter(name = "size", description = "页大小")
            , @Parameter(name = "apiCode", description = "apiCode")
            , @Parameter(name = "fileType", description = "文件类型")
    })
    public ApiResult<PageResultReturn> list(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String apiCode
            , @RequestParam(required = false) String fileType
    ) {
        return new ApiResult<PageResultReturn>().success(
                dataCleanHandlerService.configList(current, size, apiCode, fileType));
    }


    @Operation(summary = "清洗配置编辑", description = "清洗配置编辑")
    @PostMapping("/editConfig")
    public Result editConfig(@RequestBody DataCleanConfigDTO dto) {
        return dataCleanHandlerService.updateConfig(dto);

    }

    @Operation(summary = "保存配置", description = "保存配置")
    @PostMapping("/saveConfig")
    public Result saveConfig(@RequestBody DataCleanConfigDTO dto) {
        return dataCleanHandlerService.saveConfig(dto);
    }


    @Operation(summary = "获取匹配规则", description = "获取匹配规则")
    @Parameters({
            @Parameter(name = "fileHeader", description = "文件表头", required = true),
            @Parameter(name = "apiCode", description = "apiCode", required = true),
            @Parameter(name = "fileType", description = "文件类型")
    })
    @GetMapping("/getfileRules")
    public ApiResult<List<DataCleanConfigVO>> getfileRules(String fileHeader, String apiCode, String fileType) {
        return new ApiResult().fromResult(dataCleanHandlerService.getfileRules(fileHeader, apiCode, fileType), CODE_1);

    }

    @Operation(summary = "获取清洗配置", description = "获取清洗配置")
    @Parameters({
            @Parameter(name = "Id", description = "配置ID")
    })
    @GetMapping("/getRuleByID")
    public ApiResult<MarketingDataFileConfig> getRuleByID(Long Id) {

        return new ApiResult().fromResult(dataCleanHandlerService.getRuleByID(Id), CODE_1);

    }


}
