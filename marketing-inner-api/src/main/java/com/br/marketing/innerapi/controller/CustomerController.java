package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.MarketingCustomerListVO;
import com.br.marketing.vo.MarketingCustomerVO;
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
    @AddDataAuthBusiness
    public ApiResult<List<CustomerSelectVO>> getCidOrApiCodeList(@PathVariable(value = "cid", required = false) String cid) {
        List<CustomerSelectVO> list = marketingCustomerService.getCidOrApiCodeList(cid);
        return new ApiResult<List<CustomerSelectVO>>().setData(list).success();
    }


    @GetMapping("/getCustomerList")
    @ApiOperation(value = "客户信息列表数据", notes = "获取客户信息列表数据", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "name", value = "合作客户全称", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "apiCode", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "accountType",value = "账号类型0：测试；1：正式",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "accountStatus",value = "账号状态0：禁用；1：启用",paramType = "query", dataType = "string")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingCustomer.class)})
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getCustomerList(@RequestParam(defaultValue = "1") int current
                                                        , @RequestParam(defaultValue = "10") int size
                                                        , @RequestParam(required = false) String name
                                                        , @RequestParam(required = false) String apiCode
                                                        , @RequestParam(required = false) String accountType
                                                        , @RequestParam(required = false) String accountStatus
    ) {
        PageResultReturn listPage = marketingCustomerService.getCustomerList(current, size, name, apiCode,accountType, accountStatus);
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
            MarketingUserDetail user = ThreadContextInfo.getUser();
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

    @GetMapping("/getApiCodeList")
    @ApiOperation(value = "ApiCode列表,支持联想输入",notes = "ApiCode列表,支持联想输入")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "apiCode",value = "",required = false,dataType = "String")
    })
    @AddDataAuthBusiness
    public ApiResult<List<MarketingCustomerVO>> getApiCodeList(String apiCode){
        try {
            //查询
            List<MarketingCustomerVO> list = marketingCustomerService.getApiCodeList(apiCode);
            return new ApiResult<List<MarketingCustomerVO>>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage(),ex);
            return new ApiResult<List<MarketingCustomerVO>>().fail(ServiceResultEnum.FAILED);
        }
    }


    @GetMapping("/getCidOrName")
    @ApiOperation(value = "客户名称/客户编号,支持联想输入",notes = "客户名称/客户编号,支持联想输入")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "search",value = "",required = false,dataType = "String")
    })
    @AddDataAuthBusiness
    public ApiResult<List<MarketingCustomerVO>> getCidOrName(String search){
        try {
            //查询
            List<MarketingCustomerVO> list = marketingCustomerService.getCidOrName(search);
            return new ApiResult<List<MarketingCustomerVO>>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage(),ex);
            return new ApiResult<List<MarketingCustomerVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getThreeKEncryptType")
    @ApiOperation(value = "获取3key值枚举",notes = "获取3key值枚举")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "search",value = "",required = false,dataType = "String")
    })
    @AddDataAuthBusiness
    public ApiResult<String> getThreeKEncryptType(){
        try {
            return new ApiResult<String>().success().setData(marketingCustomerService.getThreeKEncryptType());
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage(),ex);
            return new ApiResult<String>().fail(ServiceResultEnum.FAILED);
        }
    }

}
