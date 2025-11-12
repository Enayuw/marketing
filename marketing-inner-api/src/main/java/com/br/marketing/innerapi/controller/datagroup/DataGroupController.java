package com.br.marketing.innerapi.controller.datagroup;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.dto.datagroup.DataGroupConfgDTO;
import com.br.marketing.vo.datagroup.DataGroupConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.br.marketing.service.datagroup.DataGroupHandlerService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据分组相关接口
 *
 * @author zhen.Li1
 * @date 2024/11/07
 */
@RestController
@RequestMapping(value = "/data/group")
@Tag(name = "数据分组相关接口", description = "数据分组相关接口")
@Slf4j
public class DataGroupController {


    @Autowired
    private DataGroupHandlerService dataGroupHandlerService;


    @GetMapping("/list")
    @Operation(summary = "数据分组配置列表", description = "数据分组配置列表")
    @Parameters({
            @Parameter(name = "ids", description = "上传记录Id,可传多个，分割"),
            @Parameter(name = "apiCode", description = "apiCode")
    })
    public ApiResult<List<DataGroupConfigVO>> list(
            @RequestParam(required = true) String ids,
            @RequestParam(required = true) String apiCode

    ) {
        return new ApiResult<List<DataGroupConfigVO>>().success(
                dataGroupHandlerService.configList(ids, apiCode));
    }


    @GetMapping("/extendField")
    @Operation(summary = "数据分组获取拓展字段", description = "数据分组获取拓展字段")
    @Parameters({
            @Parameter(name = "ids", description = "上传记录Id,可传多个，分割"),
            @Parameter(name = "apiCode", description = "apiCode")
    })
    public ApiResult<List<String>> extendField(
            @RequestParam(required = true) String ids,
            @RequestParam(required = true) String apiCode

    ) {
        return new ApiResult<List<String>>().success(
                dataGroupHandlerService.extendField(ids, apiCode));
    }


    @Operation(summary = "分组配置编辑", description = "分组配置编辑")
    @PostMapping("/editConfig")
    public ApiResult editConfig(@RequestBody DataGroupConfgDTO dto) {
        return dataGroupHandlerService.updateConfig(dto);

    }

    @Operation(summary = "新增或删除配置", description = "新增或删除配置")
    @PostMapping("/addOrDelete")
    public ApiResult addOrDeleteConfig(@RequestBody DataGroupConfgDTO dto) {
        return dataGroupHandlerService.addOrDeleteConfig(dto);
    }


    @Operation(summary = "查看字段分组进度", description = "查看字段分组进度")
    @Parameters({
            @Parameter(name = "field", description = "field", required = true),
            @Parameter(name = "id", description = "id", required = true)
    })
    @GetMapping("/getGroupFieldPercent")
    public ApiResult<Map> getGroupFieldPercent(@RequestParam(required = true) String field, @RequestParam(required = true) Long id) {
        try {
            HashMap groupPercent = dataGroupHandlerService.getGroupFieldPercent(field, id);
            return new ApiResult<Map>().success(groupPercent);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Map>().fail(ServiceResultEnum.FAILED);
        }
    }


}
