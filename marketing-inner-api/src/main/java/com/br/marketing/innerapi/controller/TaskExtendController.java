package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.service.MarketingTaskExtendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rule/taskExtend")
@Api(value = "跑分任务扩展", tags = "跑分任务扩展", produces = "application/json", consumes = "application/json", protocols = "http")
public class TaskExtendController {

    private static final Logger log = LoggerFactory.getLogger(TaskExtendController.class);

    @Autowired
    private MarketingTaskExtendService marketingTaskExtendService;

    @ApiOperation(value = "根据所选文件获得产品集合",notes = "根据所选文件获得产品集合")
    @ApiImplicitParam(name = "ids", paramType = "query", dataType = "string")
    @GetMapping("/getProducts")
    public ApiResult<List<String>> getProducts(@RequestParam(required = true) String ids){
        try {
            List<String> list = marketingTaskExtendService.getProducts(ids);
            return new ApiResult<List<String>>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage(),ex);
            return new ApiResult<List<String>>().fail(ServiceResultEnum.SUCCESS_1);
        }
    }


}
