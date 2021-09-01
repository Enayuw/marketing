package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.vo.ScoreRuleConfigPageVO;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "页号", paramType = "query", dataType = "int"
            , defaultValue = "1")
            , @ApiImplicitParam(name = "pageSize", value = "页大小", paramType = "query", dataType = "int"
            , defaultValue = "10")
            , @ApiImplicitParam(name = "search", value = "搜索：跑分规则/CID/APIcode", paramType = "query", dataType = "char")
            , @ApiImplicitParam(name = "status", value = "使用状态 1-开启；2-禁用；3-开启中", paramType = "query", dataType = "enum"
            , allowableValues = "1,2,3"
            , examples = @Example(@ExampleProperty(mediaType = "application/json"
            , value = "[{\"name\":\"开启\",\"value\":1},{\"name\":\"禁用\",\"value\":2},{\"name\":\"开启中\",\"value\":3}]")))
            , @ApiImplicitParam(name = "cts", value = "创建时间开始", paramType = "query", dataType = "dateTime")
            , @ApiImplicitParam(name = "cte", value = "创建时间结束", paramType = "query", dataType = "dateTime")
            , @ApiImplicitParam(name = "uts", value = "更新时间开始", paramType = "query", dataType = "dateTime")
            , @ApiImplicitParam(name = "ute", value = "更新时间结束", paramType = "query", dataType = "dateTime")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = ScoreRuleConfigPageVO.class)})
    public ApiResult<PageResultReturn> findListPage(@RequestParam(defaultValue = "1") int page
            , @RequestParam(defaultValue = "10") int pageSize
            , @RequestParam(required = false) String search
            , @RequestParam(required = false) Integer status
            , @RequestParam(required = false) String cts
            , @RequestParam(required = false) String cte
            , @RequestParam(required = false) String uts
            , @RequestParam(required = false) String ute
    ) {
        PageResultReturn listPage = scoreRuleConfigService.findListPage(page, pageSize, search, status, cts, cte, uts, ute);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    /**
     * 添加
     *
     * @param scoreRuleConfig 接收参数pojo
     * @return ApiResult
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/1 14:28
     */
    @PostMapping()
    public ApiResult<?> add(@RequestBody ScoreRuleConfig scoreRuleConfig) {
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.UNKNOWN_ERROR);
    }
}
