package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.innerapi.config.ThreadContextInfo;
import com.br.marketing.service.RuleOfSoleService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 去重规则控制层
 * songjuanjuan
 */
@RestController
@RequestMapping("/rule/sole")
public class RuleOfSoleContronller {

    private static final Logger log = LoggerFactory.getLogger(RuleOfSoleContronller.class);

    @Autowired
    RuleOfSoleService ruleOfSoleService;


    @GetMapping("/test")
    public String test(){
        /**
         * 获取用户上线文
         */
        UserDetail user = ThreadContextInfo.getUser();
        return "Success";
    }

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
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.UNKNOWN_ERROR);
        }
    }


    @ApiOperation(value = "判断规则名称是否重复",notes = "重复返回false；没有重复返回true")
    @ApiImplicitParam(name = "soleName",value = "规则名称",required = true,dataType = "String")
    @GetMapping("/getNameOnly")
    public boolean getNameOnly(String soleName){
        //查询
        return ruleOfSoleService.getNameOnly(soleName);
    }


    @ApiOperation(value = "匹配商户列表",notes = "支持模糊搜索")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "search",value = "",required = false,dataType = "String")
    })
    @GetMapping("/getCustomer")
    public ApiResult<List<MarketingCustomer>> getCustomer(String search){
        //返回 [{"cid":"","api_code":"123","name":"商户名称","short_name":"商户简称"},{"cid":"","api_code":"123","name":"商户名称","short_name":"商户简称"}]
        try {
            //查询
            List<MarketingCustomer> list = ruleOfSoleService.getCustomer(search);
            return new ApiResult<List<MarketingCustomer>>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage());
            return new ApiResult<List<MarketingCustomer>>().fail(ServiceResultEnum.UNKNOWN_ERROR);
        }
    }


    @ApiOperation(value = "判断商户下是否有其他规则",notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "soleId",value = "当前去重规则编码",required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "cid",value = "商户编码",required = true,dataType = "String"),
            @ApiImplicitParam(name = "apiCode",value = "",required = true,dataType = "String")
    })
    @GetMapping("/getCusOnly")
    public boolean getCusOnly(Integer soleId,String cid,String apiCode){
        //查询
        return true;
    }



    @ApiOperation(value = "新增/变更去重规则",notes = "")
    @PostMapping("/save")
    public boolean save(){
        //根据有没有id判断是新增或者变更

        //存储变更记录-->表里字段是 varchar类型，需要转换一下
        return true;
    }


    @ApiOperation(value = "查看去重规则",notes = "")
    @ApiImplicitParam(name = "id",value = "去重规则编码",required = true,dataType = "Integer")
    @GetMapping("/getById")
    public String getById(Integer id){


        return "去重规则实体类";
    }


    @ApiOperation(value = "操作去重规则",notes = "操作去重规则状态，开启/关闭")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "去重规则编码",required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "status",value = "状态(1-开启;2-禁用)",required = true,dataType = "Integer")
    })
    @GetMapping("/updateStatusById")
    public boolean updateStatusById(Integer id,Integer status){


        return true;
    }


    @ApiOperation(value = "变更记录查看",notes = "")
    @ApiImplicitParam(name = "id",value = "去重规则编码",required = true,dataType = "Integer")
    @GetMapping("/getUpdateRecord")
    public List<Object> getUpdateRecord(Integer id){
        ArrayList<Object> list = new ArrayList<>();
        //查询

        return list;
    }


    @ApiOperation(value = "匹配商户下的场景",notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cid",value = "商户编码",required = true,dataType = "String"),
            @ApiImplicitParam(name = "apiCode",value = "",required = true,dataType = "String")
    })
    @GetMapping("/getUserTypeByCid")
    public List<Object> getUserTypeByCid(String cid, String apiCode){
        ArrayList<Object> list = new ArrayList<>();
        //查询
        return list;
    }

}
