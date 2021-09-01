package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.vo.CustomerSelectVO;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 客户信息
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 15:12
 */
@RestController
@RequestMapping(value = "/rule/customer")
@Api(value = "客户信息", tags = "客户信息", produces = "application/json", consumes = "application/json", protocols = "http")
public class MarketingCustomerController {

    @Resource
    private MarketingCustomerService marketingCustomerService;


    /**
     * 获取cid、apiCode
     *
     * @param cid 合作客户id
     * @return {@link ApiResult<List<CustomerSelectVO>>}
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/1 15:14
     */
    @ApiOperation(value = "获取cid、apiCode集合", notes = "集合", httpMethod = "GET")
    @ApiImplicitParam(name = "cid", value = "合作客户id", paramType = "query", dataType = "string")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = CustomerSelectVO.class)})
    @GetMapping({"/list", "/list/{cid}"})
    public ApiResult<List<CustomerSelectVO>> getCIDList(@PathVariable(value = "cid", required = false) String cid) {
        List<CustomerSelectVO> list = marketingCustomerService.getCidOrApiCodeList(cid);
        return new ApiResult<List<CustomerSelectVO>>().setData(list).success();
    }
}
