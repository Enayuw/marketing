package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.IMarketingCustomerAssignedGroupService;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.MarketingCustomerListVO;
import com.br.marketing.vo.MarketingCustomerVO;
import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 客户信息
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 15:12
 */
@RestController
@RequestMapping(value = "/rule/customerAssignedGroup")
@Tag(name = "客户开发分组信息", description = "客户开发分组信息")
public class CustomerAssignedGroupController {

    private static final Logger log = LoggerFactory.getLogger(CustomerAssignedGroupController.class);

    @Resource
    private IMarketingCustomerAssignedGroupService marketingCustomerAssignedGroupService;


    @Operation(summary = "获取客户分组列表", description = "获取客户分组列表")
    @GetMapping("/getAssignedGroup")
    public ApiResult<Set<String>> saveOrUpdateCustomer(){
        try {
            return new ApiResult<Set<String>>().success(marketingCustomerAssignedGroupService.getAssignedGroups());
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Set<String>>().fail(Sets.newHashSet(), ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "获取客户分组列表", description = "获取客户分组列表")
    @GetMapping("/getAssignedGroupByApiCode")
    public ApiResult<String> getAssignedGroupByApiCode(@RequestParam("apiCode") String apiCode){
        try {
            String assignedGroup = marketingCustomerAssignedGroupService.getAssignedGroupByApiCode(apiCode);
            return new ApiResult<String>().success().setData(assignedGroup);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<String>().fail("", ServiceResultEnum.FAILED);
        }
    }
}
