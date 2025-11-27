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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "客户信息", description = "客户信息")
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
    @Operation(summary = "获取cid、apiCode集合", description = "集合")
    @Parameter(name = "cid", description = "合作客户id")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    @GetMapping({"/list", "/list/{cid}"})
    @AddDataAuthBusiness
    public ApiResult<List<CustomerSelectVO>> getCidOrApiCodeList(@PathVariable(value = "cid", required = false) String cid) {
        List<CustomerSelectVO> list = marketingCustomerService.getCidOrApiCodeList(cid);
        return new ApiResult<List<CustomerSelectVO>>().setData(list).success();
    }


    @GetMapping("/getCustomerList")
    @Operation(summary = "客户信息列表数据", description = "获取客户信息列表数据")
    @Parameters({@Parameter(name = "current", description = "页号")
            , @Parameter(name = "size", description = "页大小")
            , @Parameter(name = "name", description = "合作客户全称")
            , @Parameter(name = "apiCode", description = "apiCode")
            , @Parameter(name = "accountType", description = "账号类型0：测试；1：正式")
            , @Parameter(name = "accountStatus", description = "账号状态0：禁用；1：启用")
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
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


    @Operation(summary = "新增/变更用户信息", description = "新增/变更用户信息")
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

    @Operation(summary = "apiCode是否重复", description = "apiCode是否重复")
    @Parameters({@Parameter(name = "id", description = "客户配置id(编辑状态需要)")
            , @Parameter(name = "apiCode", description = "apiCode")
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
    @Operation(summary = "ApiCode列表,支持联想输入", description = "ApiCode列表,支持联想输入")
    @Parameters({
            @Parameter(name = "apiCode", description = "")
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
    @Operation(summary = "客户名称/客户编号,支持联想输入", description = "客户名称/客户编号,支持联想输入")
    @Parameters({
            @Parameter(name = "search", description = "")
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
    @Operation(summary = "获取3key值枚举", description = "获取3key值枚举")
    @Parameters({
            @Parameter(name = "search", description = "")
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
