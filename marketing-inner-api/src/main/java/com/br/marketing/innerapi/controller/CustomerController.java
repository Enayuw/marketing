package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.innerapi.config.ThreadContextInfo;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.MarketingCustomerListVO;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

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
    @ApiImplicitParam(name = "cid", value = "合作客户id", paramType = "path", dataType = "string")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = CustomerSelectVO.class)})
    @GetMapping({"/list", "/list/{cid}"})
    public ApiResult<List<CustomerSelectVO>> getCidOrApiCodeList(@PathVariable(value = "cid", required = false) String cid) {
        List<CustomerSelectVO> list = marketingCustomerService.getCidOrApiCodeList(cid);
        return new ApiResult<List<CustomerSelectVO>>().setData(list).success();
    }


    @GetMapping("/getCustomerList")
    @ApiOperation(value = "客户信息列表数据", notes = "获取客户信息列表数据", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "pageSize", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "cid", value = "合作客户id", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "apiCode", paramType = "query", dataType = "string")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingCustomer.class)})
    public ApiResult<PageResultReturn> getCustomerList(@RequestParam(defaultValue = "1") int page
                                                        , @RequestParam(defaultValue = "10") int pageSize
                                                        , @RequestParam(required = false) String cid
                                                        , @RequestParam(required = false) String apiCode) {
        PageResultReturn listPage = marketingCustomerService.getCustomerList(page, pageSize, cid, apiCode);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }


    @ApiOperation(value = "新增/变更用户信息",notes = "新增/变更用户信息")
    @PostMapping("/saveOrUpdateCustomer")
    public ApiResult<Boolean> saveOrUpdateCustomer(@RequestBody @Validated MarketingCustomerListVO vo){
        try {
            //获取用户上下文
            UserDetail user = ThreadContextInfo.getUser();
            return marketingCustomerService.saveOrUpdateCustomer(vo,user);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "apiCode是否重复",notes = "apiCode是否重复")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "客户配置id(编辑状态需要)", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "apiCode", paramType = "query", dataType = "string")
    })
    @GetMapping("/apiCodeOnly")
    public ApiResult<Boolean> apiCodeOnly(@RequestParam(required = false) String id,@RequestParam(required = true) String apiCode){
        try {
            return marketingCustomerService.apiCodeOnly(id,apiCode);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }

}
