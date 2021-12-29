package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.service.PushInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/rule/pushInfo")
@Api(value = "执行记录", tags = "执行记录", produces = "application/json", consumes = "application/json", protocols = "http")
public class PushInfoController {

    private static final Logger log = LoggerFactory.getLogger(PushInfoController.class);

    @Autowired
    private PushInfoService pushInfoService;

    @ApiOperation(value = "推送列表",notes = "推送列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "mApiCode", value = "合作客户id", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "pushBeginTime", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "pushEndTime", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "pushInfoId", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "mStatus", paramType = "query", dataType = "integer")
    })
    @GetMapping("/getPushInfoList")
    public ApiResult<PageResultReturn> getPushInfoList(@RequestParam(defaultValue = "1") int current,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) String mApiCode,
                                                       @RequestParam(required = false) String pushBeginTime,
                                                       @RequestParam(required = false) String pushEndTime,
                                                       @RequestParam(required = false) String pushInfoId,
                                                       @RequestParam(required = false) Integer mStatus){
        try {
            PageResultReturn list = pushInfoService.getPushInfoList(current,size,mApiCode,pushBeginTime,pushEndTime,pushInfoId,mStatus);
            return new ApiResult<PageResultReturn>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage(),ex);
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.SUCCESS_1);
        }
    }




}
