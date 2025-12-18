package com.br.marketing.innerapi.controller;

import com.br.marketing.client.rulecleaning.DataCleanDTO;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.service.clean.common.DataCleanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName DataCleanCommonController
 * @Author hang.zhou
 * @Date 2025/11/11
 */
@RestController
@RequestMapping("/dataClean/api")
@Tag(name = "通用数据清洗接口", description = "通用数据清洗接口")
public class DataCleanApiController {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanApiController.class);

    @Resource
    private DataCleanService dataCleanService;

    @Operation(summary = "数据清洗通用接口", description = "数据清洗通用接口")
    @PostMapping(value = "/commonClean")
    public ApiResult commonClean(@RequestBody DataCleanDTO dataCleanDTO) {
        ApiResult apiResult;
        logger.warn("数据清洗通用接口接收到请求，params:{}", dataCleanDTO);
        Result result = dataCleanService.commonClean(dataCleanDTO);
        if (result.isSuccess()) {
            apiResult = new ApiResult().success(result.getData());
        } else if (Objects.equals(ResultCode.FAIL.getValue(), result.getCode())) {
            apiResult = new ApiResult().fail(result.getMessage()).setData(result.getData());
        } else {
            apiResult = new ApiResult().setCode(String.valueOf(result.getCode())).setData(result.getData()).setMessage(result.getMessage());
        }
        logger.warn("数据清洗完成，code:{},data:{},message:{}", apiResult.getCode(), apiResult.getData(), apiResult.getMessage());
        return apiResult;
    }

}
