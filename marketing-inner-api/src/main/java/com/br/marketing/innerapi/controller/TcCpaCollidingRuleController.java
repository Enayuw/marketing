package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.tc.TcCpaCollidingRuleInfoDTO;
import com.br.marketing.dto.tc.TcCpaMagnitudeDistDTO;
import com.br.marketing.service.tccpa.TcCpaCollidingRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;


/**
 * 同程CPA撞库规则定制页面
 */
@RestController
@RequestMapping("/tcCpa/customize/collidingRule")
@Api(value = "TcCpaCollidingRuleController")
public class TcCpaCollidingRuleController {

    private static final Integer CODE_1 = 1;

    @Resource
    private TcCpaCollidingRuleService tcCpaCollidingRuleService;

    @ApiOperation(value = "同程撞库规则基础信息", notes = "同程撞库规则基础信息", httpMethod = "GET")
    @GetMapping("/info")
    public ApiResult info() {
        return new ApiResult<TcCpaCollidingRuleInfoDTO>().fromResult(tcCpaCollidingRuleService.info(), CODE_1);
    }

    @ApiOperation(value = "同程撞库规则基础信息", notes = "同程撞库规则基础信息", httpMethod = "POST")
    @PostMapping("/magnitudeDist")
    public ApiResult magnitudeDist(@RequestBody List<String> releaseTimes) {
        return new ApiResult<List<TcCpaMagnitudeDistDTO>>().fromResult(tcCpaCollidingRuleService.magnitudeDist(releaseTimes), CODE_1);
    }




}
