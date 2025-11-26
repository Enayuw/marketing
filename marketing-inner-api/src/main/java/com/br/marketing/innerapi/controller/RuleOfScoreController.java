package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.vo.ScoreRuleConfigPageVO;
import com.br.marketing.vo.ScoreRuleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 跑分配置
 */
@RestController
@RequestMapping(value = "/rule/score")
@Tag(name = "跑分配置", description = "跑分配置")
public class RuleOfScoreController {


    @Resource
    private ScoreRuleConfigService scoreRuleConfigService;


    /**
     * 跑分配置列表
     *
     * @param page     页号 {@code 1}
     * @param pageSize 页大小 {@code 10}
     * @return ApiResult {@link PageResultReturn}
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/8/30 15:52
     */
    @GetMapping("/page")
    @Operation(summary = "列表数据", description = "获取跑分配置列表数据")
    @Parameters({@Parameter(name = "current", description = "页号")
            , @Parameter(name = "size", description = "页大小")
            , @Parameter(name = "search", description = "搜索：跑分规则/CID/APIcode")
            , @Parameter(name = "status", description = "使用状态 1-开启；2-禁用；3-开启中")
            , @Parameter(name = "cts", description = "创建时间开始")
            , @Parameter(name = "cte", description = "创建时间结束")
            , @Parameter(name = "uts", description = "更新时间开始")
            , @Parameter(name = "ute", description = "更新时间结束")
            , @Parameter(name = "execType", description = "任务执行策略 1-一次性全量；3-每个任务的周期;4-每日定时")
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> findListPage(@RequestParam(name = "current", defaultValue = "1") int page
            , @RequestParam(name = "size", defaultValue = "10") int pageSize
            , @RequestParam(required = false) String search
            , @RequestParam(required = false) Integer status
            , @RequestParam(required = false) String cts
            , @RequestParam(required = false) String cte
            , @RequestParam(required = false) String uts
            , @RequestParam(required = false) String ute
            , @RequestParam(required = false) Integer execType
    ) {
        PageResultReturn listPage = scoreRuleConfigService.findListPage(page, pageSize, search, status, cts, cte, uts, ute,execType);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    /**
     * 添加
     *
     * @param scoreRuleVO 接收参数pojo
     * @return ApiResult
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/1 14:28
     */
    @Operation(summary = "添加跑分配置", description = "新增操作")
    @PostMapping("/rule")
    @Validated
    public ApiResult<?> save(@Valid @RequestBody ScoreRuleVO scoreRuleVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            FieldError fieldError = fieldErrors.get(0);
            return new ApiResult<>().fail(ServiceResultEnum.SUCCESS_1.getCode(), fieldError.getDefaultMessage());
        }
        MarketingUserDetail user = ThreadContextInfo.getUser();
        scoreRuleConfigService.save(scoreRuleVO, user);
        return new ApiResult<>().success();
    }

    /**
     * 设置开启状态 1-开启；2-禁用；3-开启中
     *
     * @param rid    规则主键
     * @param status 状态值
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/3 11:01
     */
    @Operation(summary = "设置开启状态", description = "设置开启状态 1-开启；2-禁用；3-开启中")
    @Parameters({@Parameter(name = "rid", description = "规则主键")
            , @Parameter(name = "crId", description = "规则与客户关系主键")
            , @Parameter(name = "status", description = "状态")})
    @PostMapping("/stare/{rid}/{crId}/{status}")
    public ApiResult<?> status(@PathVariable(name = "rid") Long rid
            , @PathVariable(name = "crId") Long crId
            , @PathVariable(name = "status") Integer status) {
        MarketingUserDetail user = ThreadContextInfo.getUser();
        boolean bool = scoreRuleConfigService.setStatus(rid, crId, status, user);
        if (bool) {
            return new ApiResult<>().success(true);
        }
        return new ApiResult<>().success(false, "操作失败，请稍后重试");
    }

    /**
     * 获取详情
     *
     * @param rid  规则主键
     * @param crId 规则与客户关系主键
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/3 11:18
     */
    @Operation(summary = "详情", description = "详情")
    @Parameters({@Parameter(name = "rid", description = "规则主键")
            , @Parameter(name = "crId", description = "规则与客户关系主键")})
    @GetMapping("/detail/{rid}/{crId}")
    public ApiResult<ScoreRuleVO> detail(@PathVariable(name = "rid") Long rid, @PathVariable(name = "crId") Long crId) {
        ScoreRuleVO scoreRuleVO = scoreRuleConfigService.detail(rid, crId);
        return new ApiResult<ScoreRuleVO>().success(scoreRuleVO);
    }

    /**
     * 变更
     *
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/3 14:11
     */
    @Operation(summary = "变更", description = "变更操作")
    @PostMapping("/modify")
    @Validated
    public ApiResult<?> modify(@Valid @RequestBody ScoreRuleVO scoreRuleVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            FieldError fieldError = fieldErrors.get(0);
            return new ApiResult<>().fail(ServiceResultEnum.SUCCESS_1.getCode(), fieldError.getDefaultMessage());
        }
        MarketingUserDetail user = ThreadContextInfo.getUser();
        scoreRuleConfigService.modify(scoreRuleVO, user);
        return new ApiResult<>().success();
    }

}
