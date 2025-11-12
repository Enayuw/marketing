package com.br.marketing.innerapi.controller;

import com.br.marketing.client.rulecleaning.DataCleanDTO;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.service.clean.common.DataCleanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName DataCleanCommonController
 * @Author hang.zhou
 * @Date 2025/11/11
 */
@RestController
@RequestMapping("/dataClean/api")
@Api(value = "通用数据清洗接口", tags = "通用数据清洗接口", produces = "application/json", consumes = "application/json", protocols = "http")
public class DataCleanApiController {

    @Resource
    private DataCleanService dataCleanService;

    @ApiOperation(value = "数据清洗通用接口", notes = "数据清洗通用接口")
    @PostMapping(name = "/commonClean")
    public ApiResult commonClean(@RequestBody DataCleanDTO dataCleanDTO) {

        Result result = dataCleanService.commonClean(dataCleanDTO);
        return new ApiResult().setCode(String.valueOf(result.getCode())).setMessage(result.getMessage()).setData(result.getData());

    }

}
