package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.innerapi.config.ThreadContextInfo;
import com.br.marketing.service.RuleOfSoleService;
import com.br.marketing.vo.SoleOptLogVO;
import com.br.marketing.vo.SoleRuleDetailVO;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        } catch (Exception ex) {
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
        //返回 [{"id":"","cid":"","api_code":"123","name":"商户名称","short_name":"商户简称"},{}]
        try {
            //查询
            List<MarketingCustomer> list = ruleOfSoleService.getCustomer(search);
            return new ApiResult<List<MarketingCustomer>>().success(list);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new ApiResult<List<MarketingCustomer>>().fail(ServiceResultEnum.UNKNOWN_ERROR);
        }
    }


    @ApiOperation(value = "判断商户是否已经被其他规则匹配",notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "soleId",value = "当前去重规则编码",required = true,dataType = "Long"),
            @ApiImplicitParam(name = "customerId",value = "商户编码",required = true,dataType = "Long")
    })
    @GetMapping("/getCusOnly")
    public boolean getCusUserType(Long soleId, Long customerId){
        //查询
        return ruleOfSoleService.getCusUserType(soleId,customerId);
    }



    @ApiOperation(value = "新增/变更去重规则",notes = "")
    @PostMapping("/save")
    public boolean save(@RequestBody SoleRuleDetailVO vo){
        //根据有没有id判断是新增或者变更

        //变更的时候还需要 往日志表添加信息，添加此条更改之前的规则记录
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
            @ApiImplicitParam(name = "id",value = "去重规则编码",required = true,dataType = "Long"),
            @ApiImplicitParam(name = "status",value = "状态(1-开启;2-禁用)",required = true,dataType = "Integer")
    })
    @GetMapping("/updateStatusById")
    public boolean updateStatusById(Long id,Integer status){
        return ruleOfSoleService.updateStatusById(id,status);
    }


    @ApiOperation(value = "变更记录查看",notes = "")
    @ApiImplicitParam(name = "id",value = "去重规则编码",required = true,dataType = "Long")
    @GetMapping("/getUpdateRecord")
    public ApiResult<List<SoleOptLogVO>> getUpdateRecord(Long id){
        //查询
        try {
            List<SoleOptLogVO> list = ruleOfSoleService.getUpdateRecord(id);
            return new ApiResult<List<SoleOptLogVO>>().success(list);
        }catch (Exception ex){
            log.error(ex.getMessage());
            return new ApiResult<List<SoleOptLogVO>>().fail(ServiceResultEnum.UNKNOWN_ERROR);
        }
    }


   /* @ApiOperation(value = "匹配商户下的场景",notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cid",value = "商户编码",required = true,dataType = "String"),
            @ApiImplicitParam(name = "apiCode",value = "",required = true,dataType = "String")
    })
    @GetMapping("/getUserTypeByCid")
    public List<Object> getUserTypeByCid(String cid, String apiCode){
        ArrayList<Object> list = new ArrayList<>();
        //查询
        return list;
    }*/

}
