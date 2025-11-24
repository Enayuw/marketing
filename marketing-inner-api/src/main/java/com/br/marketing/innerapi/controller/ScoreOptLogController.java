package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.ScoreOptLog;
import com.br.marketing.service.ScoreOptLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "跑分配置变更记录", description = "跑分配置变更记录")
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
    @Operation(summary = "列表数据", description = "跑分配置变更记录列表数据")
    @Parameters({@Parameter(name = "current", description = "页号")
            , @Parameter(name = "size", description = "页大小")
            , @Parameter(name = "rid", description = "配置主键")
            , @Parameter(name = "cid", description = "客户id")
            , @Parameter(name = "apiCode", description = "接口编号")
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    public ApiResult<PageResultReturn> findListPage(@RequestParam(name = "current", defaultValue = "1") int page
            , @RequestParam(name = "size", defaultValue = "10") int pageSize
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
