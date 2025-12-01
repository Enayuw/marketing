package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.enums.TcFailMsgEnum;
import com.br.marketing.service.tccpa.TcCpaDataDeleteRuleService;
import com.br.marketing.vo.tccpa.TcyrCpaDeleteRuleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 营销平台筛选接口
 */
@RestController
@RequestMapping("/tcCpa/customize/deleteRule")
@Api(value = "TcCpaCustomizeController")
public class TcCpaDeleteRuleController {

    private static final Integer CODE_1 = 1;

    @Resource
    private TcCpaDataDeleteRuleService tcCpaDataDeleteRuleService;

    /**
     * 同程CPA跑分文件数据包删除
     * @return
     */
    @ApiOperation(value = "同程剔除规则列表 ", notes = "同程剔除规则列表 ", httpMethod = "GET")
    @GetMapping("/page")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "packageName", value = "包名称", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "status", value = "状态", paramType = "query", dataType = "integer")
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
    @ApiOperation(value = "同程剔除规则启用/禁用", notes = "同程剔除规则启用/禁用", httpMethod = "POST")
    @PostMapping("/enable")
    public ApiResult enable(@RequestBody TcyrCpaDeleteRuleVO deleteRuleVO) {
        return new ApiResult().fromResult(tcCpaDataDeleteRuleService.enable(deleteRuleVO.getId(), deleteRuleVO.getEnabled()), CODE_1);
    }



    /**
     * 同程CPA跑分文件数据包删除
     * @return
     */
    @ApiOperation(value = "同程CPA剔除规则删除", notes = "同程CPA剔除规则删除", httpMethod = "GET")
    @GetMapping("/delete")
    public ApiResult delete(@RequestParam("id") Long id) {
        return new ApiResult().fromResult(tcCpaDataDeleteRuleService.delete(id), CODE_1);
    }

    /**
     * 同程CPA跑分文件数据包删除
     * @return
     */
    @ApiOperation(value = "获取FailMsg列表", notes = "获取FailMsg列表", httpMethod = "GET")
    @GetMapping("/getFailMsgs")
    public ApiResult getFailMsgs() {
        List<String> failMsgs = Arrays.stream(TcFailMsgEnum.values()).map(TcFailMsgEnum::getValue).collect(Collectors.toList());
        return new ApiResult().success().setData(failMsgs);
    }

    /**
     * 同程CPA跑分文件数据包删除
     * @return
     */
    @ApiOperation(value = "同程剔除规则新增", notes = "同程剔除规则新增", httpMethod = "POST")
    @PostMapping("/rule")
    public ApiResult rule(@RequestBody TcyrCpaDeleteRuleVO ruleVO) {
        return new ApiResult().fromResult(tcCpaDataDeleteRuleService.rule(ruleVO), CODE_1);
    }

}
