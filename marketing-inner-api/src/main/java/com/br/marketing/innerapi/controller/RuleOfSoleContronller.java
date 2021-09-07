package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.innerapi.config.ThreadContextInfo;
import com.br.marketing.service.RuleOfSoleService;
import com.br.marketing.vo.MarketingCustomerVO;
import com.br.marketing.vo.SoleOptLogVO;
import com.br.marketing.vo.SoleRuleDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 去重规则控制层
 * songjuanjuan
 */
@RestController
@Configuration
@RequestMapping("/rule/sole")
@Api(value = "API跑分前数据去重配置",tags = "API跑分前数据去重配置", produces = "application/json", consumes = "application/json", protocols = "http")
public class RuleOfSoleContronller {

    private static final Logger log = LoggerFactory.getLogger(RuleOfSoleContronller.class);

    @Autowired
    RuleOfSoleService ruleOfSoleService;

    @ApiOperation(value = "去重规则列表",notes = "")
    @PostMapping("/list")
    public ApiResult<PageResultReturn> list(@RequestBody SoleRuleSearchDTO dto,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int pageSize){
        try {
            PageResultReturn list = ruleOfSoleService.list(dto, page, pageSize);
            return new ApiResult<PageResultReturn>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage());
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.SUCCESS_1);
        }
    }


    @ApiOperation(value = "判断规则名称是否重复",notes = "如果编辑状态需要传soleId")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "soleName",value = "规则名称",required = true,dataType = "String"),
            @ApiImplicitParam(name = "soleId",value = "当前规则id",required = false,dataType = "String")
    })
    @GetMapping("/getNameOnly")
    public boolean getNameOnly(String soleName,String soleId){
        //查询
        return ruleOfSoleService.getNameOnly(soleName,soleId);
    }


    @ApiOperation(value = "匹配商户列表",notes = "支持模糊搜索")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "search",value = "",required = false,dataType = "String")
    })
    @GetMapping("/getCustomer")
    public ApiResult<List<MarketingCustomerVO>> getCustomer(String search){
        //返回 [{"id":"","cid":"","api_code":"123","name":"商户名称","short_name":"商户简称"},{}]
        try {
            //查询
            List<MarketingCustomerVO> list = ruleOfSoleService.getCustomer(search);
            return new ApiResult<List<MarketingCustomerVO>>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage());
            return new ApiResult<List<MarketingCustomerVO>>().fail(ServiceResultEnum.SUCCESS_1);
        }
    }


    @ApiOperation(value = "判断商户是否已经被其他规则匹配",notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "soleId",value = "当前去重规则编码",required = false,dataType = "String"),
            @ApiImplicitParam(name = "customerId",value = "商户编码",required = true,dataType = "String")
    })
    @GetMapping("/getCusOnly")
    public boolean getCusOnly(String soleId, String customerId){
        //查询
        return ruleOfSoleService.getCusUserType(soleId,customerId);
    }



    @ApiOperation(value = "新增/变更去重规则",notes = "")
    @PostMapping("/saveOrUpdate")
    public boolean saveOrUpdate(@RequestBody @Validated SoleRuleDetailVO vo){
        //获取用户上下文
        UserDetail user = ThreadContextInfo.getUser();
        return ruleOfSoleService.saveOrUpdate(vo,user);
    }


    @ApiOperation(value = "查看去重规则",notes = "")
    @ApiImplicitParam(name = "id",value = "去重规则编码",required = true,dataType = "String")
    @GetMapping("/getSoleById")
    public ApiResult<SoleRuleDetailVO> getSoleById(String id){
        try {
            SoleRuleDetailVO soleRuleDetailVO = ruleOfSoleService.getSoleById(id);
            return new ApiResult<SoleRuleDetailVO>().success(soleRuleDetailVO);
        }catch (Exception ex){
            log.error(ex.getMessage());
            return new ApiResult<SoleRuleDetailVO>().fail(ServiceResultEnum.FAILED);
        }
    }


    @ApiOperation(value = "操作去重规则",notes = "操作去重规则状态，开启/关闭")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "去重规则编码",required = true,dataType = "String"),
            @ApiImplicitParam(name = "status",value = "状态(1-开启;2-禁用)",required = true,dataType = "Integer")
    })
    @GetMapping("/updateStatusById")
    public boolean updateStatusById(String id,Integer status){
        return ruleOfSoleService.updateStatusById(id,status);
    }


    @ApiOperation(value = "变更记录查看",notes = "")
    @ApiImplicitParam(name = "id",value = "去重规则编码",required = true,dataType = "String")
    @GetMapping("/getUpdateRecord")
    public ApiResult<List<SoleOptLogVO>> getUpdateRecord(String id){
        //查询
        try {
            List<SoleOptLogVO> list = ruleOfSoleService.getUpdateRecord(id);
            return new ApiResult<List<SoleOptLogVO>>().success(list);
        }catch (Exception ex){
            log.error(ex.getMessage());
            return new ApiResult<List<SoleOptLogVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

}
