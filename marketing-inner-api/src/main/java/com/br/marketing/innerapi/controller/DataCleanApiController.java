package com.br.marketing.innerapi.controller;

import com.br.marketing.client.rulecleaning.DataCleanDTO;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.service.clean.common.DataCleanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(DataCleanApiController.class);

    @Resource
    private DataCleanService dataCleanService;

    @ApiOperation(value = "数据清洗通用接口", notes = "数据清洗通用接口")
    @PostMapping(value = "/commonClean")
    public ApiResult commonClean(@RequestBody DataCleanDTO dataCleanDTO) {
        logger.warn("数据清洗通用接口接收到请求，params:{}", dataCleanDTO);
        Result result = dataCleanService.commonClean(dataCleanDTO);
        logger.warn("数据清洗完成，清洗结果:{}",result.getData());
        if (result.isSuccess()) {
            return new ApiResult().success(result.getData());
        }
        return new ApiResult().fail(result.getMessage()).setData(result.getData());
    }

}
