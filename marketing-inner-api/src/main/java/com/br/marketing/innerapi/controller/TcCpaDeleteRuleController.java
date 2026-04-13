package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.config.biz.TcyrCpaConfigManager;
import com.br.marketing.service.tccpa.TcCpaDataDeleteRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.tccpa.TcyrCpaDeleteRuleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;


/**
 * 营销平台筛选接口
 */
@RestController
@RequestMapping("/tcCpa/customize/deleteRule")
@Tag(name = "TcCpaCustomizeController")
public class TcCpaDeleteRuleController {

    private static final Integer CODE_1 = 1;

    @Resource
    private TcCpaDataDeleteRuleService tcCpaDataDeleteRuleService;

    @Resource
    TcyrCpaConfigManager tcyrCpaConfigManager;

    /**
     * 同程CPA跑分文件数据包删除
     * @return
     */
    @Operation(summary = "同程剔除规则列表 ", description = "同程剔除规则列表 ")
    @GetMapping("/page")
    @Parameters({@Parameter(name = "current", description = "页号")
            , @Parameter(name = "size", description = "页大小")
            , @Parameter(name = "ruleName", description = "包名称")
            , @Parameter(name = "enabled", description = "状态")
    })
    public ApiResult<PageResultReturn> page(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String ruleName
            , @RequestParam(required = false) Integer enabled) {
        return new ApiResult<PageResultReturn>().success(tcCpaDataDeleteRuleService.page(current, size, ruleName, enabled));
    }


    /**
     * 同程CPA跑分文件数据包新增修改
     * @param deleteRuleVO
     * @return
     */
    @Operation(summary = "同程剔除规则启用/禁用", description = "同程剔除规则启用/禁用")
    @PostMapping("/enable")
    public ApiResult enable(@RequestBody TcyrCpaDeleteRuleVO deleteRuleVO) {
        return new ApiResult().fromResult(tcCpaDataDeleteRuleService.enable(deleteRuleVO.getId(), deleteRuleVO.getEnabled()), CODE_1);
    }



    /**
     * 同程CPA跑分文件数据包删除
     * @return
     */
    @Operation(summary = "同程CPA剔除规则删除", description = "同程CPA剔除规则删除")
    @GetMapping("/delete")
    public ApiResult delete(@RequestParam("id") Long id) {
        return new ApiResult().fromResult(tcCpaDataDeleteRuleService.delete(id), CODE_1);
    }

    /**
     * 同程CPA跑分文件数据包删除
     * @return
     */
    @Operation(summary = "获取FailMsg列表", description = "获取FailMsg列表")
    @GetMapping("/getFailMsgs")
    public ApiResult getFailMsgs() {
        return new ApiResult().success().setData(tcyrCpaConfigManager.getFailMsgVOs());
    }

    /**
     * 同程CPA跑分文件数据包删除
     * @return
     */
    @Operation(summary = "同程剔除规则新增", description = "同程剔除规则新增")
    @PostMapping("/rule")
    public ApiResult rule(@RequestBody TcyrCpaDeleteRuleVO ruleVO) {
        return new ApiResult().fromResult(tcCpaDataDeleteRuleService.rule(ruleVO), CODE_1);
    }

}
