package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.PushInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/rule/pushInfo")
@Tag(name = "执行记录", description = "执行记录")
public class PushInfoController {

    private static final Logger log = LoggerFactory.getLogger(PushInfoController.class);

    @Autowired
    private PushInfoService pushInfoService;

    @Operation(summary = "推送列表", description = "推送列表")
    @PostMapping("/getPushInfoList")
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getPushInfoList(@RequestBody PushInfoFilterDTO dto) {
        PageResultReturn list = pushInfoService.getPushInfoList(dto);
        return new ApiResult<PageResultReturn>().success(list);
    }


}
