package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleDTO;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleInfoDTO;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleQueryDTO;
import com.br.marketing.dto.tccpa.TcyrFailMsgSupplyGroupDTO;
import com.br.marketing.service.tccpa.TcCpaCollidingRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(summary = "同程CPAreleaseTime量级分布查询", description = "同程CPAreleaseTime量级分布查询")
    @PostMapping("/magnitudeDist")
    public ApiResult magnitudeDist(@RequestParam("releaseTimes") String releaseTimes,
                                   @RequestParam(name = "taskId", required = false) Long taskId) {
        return new ApiResult<List<TcyrFailMsgSupplyGroupDTO>>()
                .fromResult(tcCpaCollidingRuleService.magnitudeDist(releaseTimes, taskId), CODE_1);
    }

    @Operation(summary = "同程CPA撞库规则新增", description = "同程CPA撞库规则新增")
    @PostMapping("/rule")
    public ApiResult rule(@RequestBody @Valid TcCpaCollidingRuleDTO ruleDTO) {
        return new ApiResult().fromResult(tcCpaCollidingRuleService.rule(ruleDTO), CODE_1);
    }

    @Operation(summary = "同程CPA撞库规则列表查询", description = "同程CPA撞库规则列表查询")
    @GetMapping("/list")
    public ApiResult<PageResultReturn> list(@Valid TcCpaCollidingRuleQueryDTO dto) {
        return new ApiResult<PageResultReturn>().success(tcCpaCollidingRuleService.list(dto));
    }

    @Operation(summary = "同程CPA撞库规则修改", description = "同程CPA撞库规则修改")
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Valid TcCpaCollidingRuleDTO ruleDTO) {
        return new ApiResult().fromResult(tcCpaCollidingRuleService.update(ruleDTO), CODE_1);
    }

    @Operation(summary = "同程CPA撞库规则修改启用/禁用", description = "同程CPA撞库规则修改启用/禁用")
    @PostMapping("/enable")
    public ApiResult enable(@RequestParam("taskId") Long taskId,
                            @RequestParam("enabled") Integer enabled) {
        return new ApiResult().fromResult(tcCpaCollidingRuleService.enable(taskId, enabled), CODE_1);
    }


}
