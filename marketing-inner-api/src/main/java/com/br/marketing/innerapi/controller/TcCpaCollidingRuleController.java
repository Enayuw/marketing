package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleDTO;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleInfoDTO;
import com.br.marketing.dto.tccpa.TcyrFailMsgSupplyGroupDTO;
import com.br.marketing.service.tccpa.TcCpaCollidingRuleService;
import com.br.marketing.vo.tccpa.TcyrCpaDeleteRuleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
    public ApiResult magnitudeDist(@RequestParam("releaseTimes") String releaseTimes,
                                   @RequestParam(name = "taskId", required = false) Long taskId) {
        return new ApiResult<List<TcyrFailMsgSupplyGroupDTO>>().fromResult(tcCpaCollidingRuleService.magnitudeDist(releaseTimes, taskId), CODE_1);
    }

    @Operation(summary = "同程CPA撞库规则新增", description = "同程CPA撞库规则新增")
    @PostMapping("/rule")
    public ApiResult rule(@RequestBody @Valid TcCpaCollidingRuleDTO ruleDTO) {
        return new ApiResult().fromResult(tcCpaCollidingRuleService.rule(ruleDTO), CODE_1);
    }

    @ApiOperation(value = "同程CPA撞库规则列表查询", notes = "同程CPA撞库规则列表查询", httpMethod = "GET")
    @GetMapping("/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1"),
            @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10"),
            @ApiImplicitParam(name = "packageName", value = "数据包名称", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "enabled", value = "任务状态", paramType = "query", dataType = "integer")
    })
    public ApiResult<PageResultReturn> list(@RequestParam(defaultValue = "1") int current,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String packageName,
                                            @RequestParam(required = false) Integer enabled) {
        return new ApiResult<PageResultReturn>().success(
                tcCpaCollidingRuleService.list(current, size, packageName, enabled));
    }

    @ApiOperation(value = "同程CPA撞库规则修改", notes = "同程CPA撞库规则修改", httpMethod = "POST")
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Valid TcCpaCollidingRuleDTO ruleDTO) {
        return new ApiResult().fromResult(tcCpaCollidingRuleService.update(ruleDTO), CODE_1);
    }

    @ApiOperation(value = "同程CPA撞库规则修改启用/禁用", notes = "同程CPA撞库规则修改启用/禁用", httpMethod = "POST")
    @PostMapping("/enable")
    public ApiResult enable(@RequestParam("taskId") Long taskId,
                            @RequestParam("enabled") Integer enabled) {
        return new ApiResult().fromResult(tcCpaCollidingRuleService.enable(taskId, enabled), CODE_1);
    }


}
