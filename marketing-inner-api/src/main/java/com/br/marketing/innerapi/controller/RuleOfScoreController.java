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
import io.swagger.annotations.*;
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
@Api(value = "跑分配置", tags = "跑分配置", produces = "application/json", consumes = "application/json", protocols = "http")
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
    @ApiOperation(value = "列表数据", notes = "获取跑分配置列表数据", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer"
            , defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer"
            , defaultValue = "10")
            , @ApiImplicitParam(name = "search", value = "搜索：跑分规则/CID/APIcode", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "status", value = "使用状态 1-开启；2-禁用；3-开启中", paramType = "query", dataType = "enum"
            , allowableValues = "1,2,3")
            , @ApiImplicitParam(name = "cts", value = "创建时间开始", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "cte", value = "创建时间结束", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "uts", value = "更新时间开始", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "ute", value = "更新时间结束", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "execType", value = "任务执行策略 1-一次性全量；3-每个任务的周期;4-每日定时", paramType = "query", dataType = "Integer")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = ScoreRuleConfigPageVO.class)})
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
    @ApiOperation(value = "添加跑分配置", notes = "新增操作", httpMethod = "POST")
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
    @ApiOperation(value = "设置开启状态", notes = "设置开启状态 1-开启；2-禁用；3-开启中", httpMethod = "POST")
    @ApiImplicitParams({@ApiImplicitParam(name = "rid", value = "规则主键", paramType = "path", dataType = "long")
            , @ApiImplicitParam(name = "crId", value = "规则与客户关系主键", paramType = "path", dataType = "long")
            , @ApiImplicitParam(name = "status", value = "状态", paramType = "path", dataType = "integer")})
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
    @ApiOperation(value = "详情", notes = "详情", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "rid", value = "规则主键", paramType = "path", dataType = "long")
            , @ApiImplicitParam(name = "crId", value = "规则与客户关系主键", paramType = "path", dataType = "long")})
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
    @ApiOperation(value = "变更", notes = "变更操作", httpMethod = "POST")
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
