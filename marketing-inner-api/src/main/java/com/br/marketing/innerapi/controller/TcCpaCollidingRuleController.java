package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleDTO;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleInfoDTO;
import com.br.marketing.dto.tc.TcCpaMagnitudeDistDTO;
import com.br.marketing.service.tccpa.TcCpaCollidingRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;


/**
 * 同程CPA撞库规则定制页面
 */
@RestController
@RequestMapping("/tcCpa/customize/collidingRule")
@Tag(name = "同程CPA撞库规则定制页面", description = "同程CPA撞库规则定制页面")
public class TcCpaCollidingRuleController {

    private static final Integer CODE_1 = 1;

    @Resource
    private TcCpaCollidingRuleService tcCpaCollidingRuleService;

    @Operation(summary = "同程撞库规则基础信息", description = "同程撞库规则基础信息")
    @GetMapping("/info")
    public ApiResult info() {
        return new ApiResult<TcCpaCollidingRuleInfoDTO>().fromResult(tcCpaCollidingRuleService.info(), CODE_1);
    }

    @Operation(summary = "同程撞库规则基础信息", description = "同程撞库规则基础信息")
    @PostMapping("/magnitudeDist")
    public ApiResult magnitudeDist(@RequestParam("releaseTimes") String releaseTimes) {
        return new ApiResult<List<TcCpaMagnitudeDistDTO>>().fromResult(tcCpaCollidingRuleService.magnitudeDist(releaseTimes), CODE_1);
    }

    @Operation(summary = "同程CPA撞库规则新增", description = "同程CPA撞库规则新增")
    @PostMapping("/rule")
    public ApiResult rule(@RequestBody @Valid TcCpaCollidingRuleDTO ruleDTO) {
        return new ApiResult().fromResult(tcCpaCollidingRuleService.rule(ruleDTO), CODE_1);
    }


}
