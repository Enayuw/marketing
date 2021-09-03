package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.service.VariableDicService;
import com.br.marketing.vo.VariableDicSelectVO;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 客户配置变量值字典
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 17:40
 */
@RestController
@RequestMapping(value = "/rule/vd")
@Api(value = "客户配置变量值", tags = "客户配置变量值字典", produces = "application/json", consumes = "application/json", protocols = "http")
public class VariableDicController {

    @Resource
    private VariableDicService variableDicService;


    /**
     * 获取配置变量值字典集合
     *
     * @param cid     合作客户id
     * @param apiCode 接口编号
     * @return {@link List<VariableDicSelectVO>}
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/1 15:14
     */
    @ApiOperation(value = "配置变量值字典", notes = "集合", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "cid", value = "合作客户id", paramType = "path", dataType = "string")
            , @ApiImplicitParam(name = "apiCode", value = "接口编号", paramType = "path", dataType = "string")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = VariableDicSelectVO.class)})
    @GetMapping({"/list/{cid}/{apiCode}"})
    public ApiResult<List<VariableDicSelectVO>> findListByCidAndApiCode(@PathVariable(value = "cid") String cid
            , @PathVariable(value = "apiCode") String apiCode) {
        List<VariableDicSelectVO> list = variableDicService.findListByCidAndApiCode(cid, apiCode);
        return new ApiResult<List<VariableDicSelectVO>>().setData(list).success();
    }
}
