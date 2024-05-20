package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.PushInfoService;
import io.swagger.annotations.Api;
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

    @ApiOperation(value = "推送列表", notes = "推送列表")
    @PostMapping("/getPushInfoList")
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getPushInfoList(@RequestBody PushInfoFilterDTO dto) {
        PageResultReturn list = pushInfoService.getPushInfoList(dto);
        return new ApiResult<PageResultReturn>().success(list);
    }


}
