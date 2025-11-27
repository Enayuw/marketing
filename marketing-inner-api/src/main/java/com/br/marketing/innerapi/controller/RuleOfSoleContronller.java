package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.RuleOfSoleService;
import com.br.marketing.vo.MarketingCustomerVO;
import com.br.marketing.vo.SoleRuleDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 去重规则控制层
 * songjuanjuan
 */
@RestController
@Configuration
@RequestMapping("/rule/sole")
@Tag(name = "API跑分前数据去重配置", description = "API跑分前数据去重配置")
public class RuleOfSoleContronller {

    private static final Logger log = LoggerFactory.getLogger(RuleOfSoleContronller.class);

    @Autowired
    RuleOfSoleService ruleOfSoleService;

    @Operation(summary = "去重规则列表", description = "")
    @GetMapping("/list")
    @Parameters({@Parameter(name = "current", description = "页号")
            , @Parameter(name = "size", description = "页大小")
            , @Parameter(name = "soleName", description = "去重规则名称")
            , @Parameter(name = "status", description = "状态")
            , @Parameter(name = "apiCodes", description = "apiCode筛选,支持多选,逗号分隔")
            , @Parameter(name = "createTimeStart", description = "创建日期开始")
            , @Parameter(name = "createTimeEnd", description = "创建日期截止")
            , @Parameter(name = "updateTimeStart", description = "更新日期开始")
            , @Parameter(name = "updateTimeEnd", description = "更新日期截止")
    })
    public ApiResult<PageResultReturn> list(@RequestParam(defaultValue = "1") int current
                                            , @RequestParam(defaultValue = "10") int size
                                            , @RequestParam(required = false) String soleName
                                            , @RequestParam(required = false) Integer status
                                            , @RequestParam(required = false) String apiCodes
                                            , @RequestParam(required = false) String createTimeStart
                                            , @RequestParam(required = false) String createTimeEnd
                                            , @RequestParam(required = false) String updateTimeStart
                                            , @RequestParam(required = false) String updateTimeEnd
                                            ){
        try {
            PageResultReturn list = ruleOfSoleService.list(current, size,soleName,status,apiCodes,
                    createTimeStart,createTimeEnd,updateTimeStart,updateTimeEnd);
            return new ApiResult<PageResultReturn>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage(),ex);
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.SUCCESS_1);
        }
    }


    @Operation(summary = "判断规则名称是否重复,是否合法", description = "如果编辑状态需要传soleId")
    @Parameters({
            @Parameter(name = "soleName", description = "规则名称", required = true),
            @Parameter(name = "soleId", description = "当前规则id", required = false)
    })
    @GetMapping("/getNameOnly")
    public ApiResult<Boolean> getNameOnly(String soleName,String soleId){
        //查询
        try {
            boolean flag = ruleOfSoleService.getNameOnly(soleName, soleId);
            if(flag){
                return new ApiResult<Boolean>().success(true);
            }else {
                return new ApiResult<Boolean>().success(false,"名称重复或者不合法，请重新输入!");
            }

        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }

    }


    @Operation(summary = "匹配商户列表", description = "支持模糊搜索")
    @Parameters({
            @Parameter(name = "search", description = "", required = false)
    })
    @GetMapping("/getCustomer")
    @AddDataAuthBusiness
    public ApiResult<List<MarketingCustomerVO>> getCustomer(String search){
        //返回 [{"id":"","cid":"","api_code":"123","name":"商户名称","short_name":"商户简称"},{}]
        try {
            //查询
            List<MarketingCustomerVO> list = ruleOfSoleService.getCustomer(search);
            return new ApiResult<List<MarketingCustomerVO>>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage(),ex);
            return new ApiResult<List<MarketingCustomerVO>>().fail(ServiceResultEnum.SUCCESS_1);
        }
    }

    //新增接口
    @Operation(summary = "根据商户查询usertype", description = "支持多个同时查询,返回参数格式适应前端")
    @PostMapping("/getUserByCus")
    public ApiResult<List<Map>> getUserByCus(@RequestBody List<MarketingCustomerVO> customerVOs){
        try {
            //查询
            List<Map> list = ruleOfSoleService.getUserByCus(customerVOs);
            return new ApiResult<List<Map>>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage(),ex);
            return new ApiResult<List<Map>>().fail(ServiceResultEnum.SUCCESS_1);
        }
    }


    @Operation(summary = "新增/变更去重规则", description = "")
    @PostMapping("/saveOrUpdate")
    public ApiResult<Boolean> saveOrUpdate(@RequestBody @Validated SoleRuleDetailVO vo){
        //获取用户上下文
        try {
            MarketingUserDetail user = ThreadContextInfo.getUser();
            return ruleOfSoleService.saveOrUpdate(vo,user);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }


    @Operation(summary = "查看去重规则", description = "")
    @Parameter(name = "id", description = "去重规则编码", required = true)
    @GetMapping("/getSoleById")
    public ApiResult<SoleRuleDetailVO> getSoleById(String id){
        try {
            SoleRuleDetailVO soleRuleDetailVO = ruleOfSoleService.getSoleById(id);
            return new ApiResult<SoleRuleDetailVO>().success(soleRuleDetailVO);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<SoleRuleDetailVO>().fail(ServiceResultEnum.FAILED);
        }
    }


    @Operation(summary = "操作去重规则", description = "操作去重规则状态，开启/关闭")
    @Parameters({
            @Parameter(name = "id", description = "去重规则编码", required = true),
            @Parameter(name = "status", description = "状态(1-开启;2-禁用)", required = true)
    })
    @GetMapping("/updateStatusById")
    public ApiResult<Boolean> updateStatusById(String id,Integer status){
        //查询
        try {
            MarketingUserDetail user = ThreadContextInfo.getUser();
            boolean flag = ruleOfSoleService.updateStatusById(id,status,user);
            if(flag){
                return new ApiResult<Boolean>().success(true,"操作成功！");
            }else {
                return new ApiResult<Boolean>().success(false,"操作失败！");
            }
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }


    @Operation(summary = "变更记录查看", description = "支持分页")
    @Parameter(name = "id", description = "去重规则编码", required = true)
    @GetMapping("/getUpdateRecord")
    public ApiResult<PageResultReturn> getUpdateRecord(@RequestParam(required = true) String id,
                                                       @RequestParam(defaultValue = "1") int current,
                                                       @RequestParam(defaultValue = "10") int size){
        //查询
        try {
            PageResultReturn list = ruleOfSoleService.getUpdateRecord(id,current,size);
            return new ApiResult<PageResultReturn>().success(list);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
        }
    }

}
