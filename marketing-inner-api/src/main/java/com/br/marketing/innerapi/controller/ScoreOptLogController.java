package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.ScoreOptLog;
import com.br.marketing.service.ScoreOptLogService;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 跑分配置变更记录
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/6 13:34
 */
@RestController
@RequestMapping(value = "/rule/score/optlog")
@Api(value = "跑分配置变更记录", tags = "跑分配置变更记录", produces = "application/json", consumes = "application/json", protocols = "http")
public class ScoreOptLogController {

    @Resource
    private ScoreOptLogService scoreOptLogService;


    /**
     * 跑分配置变更记录列表
     *
     * @param page     页号 {@code 1}
     * @param pageSize 页大小 {@code 10}
     * @return ApiResult {@link PageResultReturn}
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/8/30 15:52
     */
    @GetMapping("/page")
    @ApiOperation(value = "列表数据", notes = "跑分配置变更记录列表数据", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "页号", paramType = "query", dataType = "integer"
            , defaultValue = "1")
            , @ApiImplicitParam(name = "pageSize", value = "页大小", paramType = "query", dataType = "integer"
            , defaultValue = "10")
            , @ApiImplicitParam(name = "rid", value = "配置主键", paramType = "query", dataType = "long")
            , @ApiImplicitParam(name = "cid", value = "客户id", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "apiCode", value = "接口编号", paramType = "query", dataType = "string")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = ScoreOptLog.class)})
    public ApiResult<PageResultReturn> findListPage(@RequestParam(defaultValue = "1") int page
            , @RequestParam(defaultValue = "10") int pageSize
            , @RequestParam Long rid
            , @RequestParam String cid
            , @RequestParam String apiCode
    ) {
        PageResultReturn listPage = scoreOptLogService.findListPage(page, pageSize, rid, cid, apiCode);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }
}
