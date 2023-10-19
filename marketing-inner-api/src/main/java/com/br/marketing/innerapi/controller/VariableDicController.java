package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.VariableDicService;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.VariableDicListVO;
import com.br.marketing.vo.VariableDicSelectVO;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

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


    @GetMapping("/getVariableDicList")
    @ApiOperation(value = "客户配置变量值列表数据", notes = "客户配置变量值列表数据", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "cid", value = "合作客户id", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "apiCode", paramType = "query", dataType = "string")
    })
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getVariableDicList(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String cid
            , @RequestParam(required = false) String apiCode) {
        PageResultReturn listPage = variableDicService.getVariableDicList(current, size, cid, apiCode);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }


    @ApiOperation(value = "新增/变更客户配置变量值字典",notes = "新增/变更客户配置变量值字典")
    @PostMapping("/saveOrUpdateVariableDic")
    public ApiResult<Boolean> saveOrUpdateVariableDic(@RequestBody @Validated VariableDicListVO vo
            ,@RequestParam(defaultValue = "30") Integer days){
        try {
            //获取用户上下文
            MarketingUserDetail user = ThreadContextInfo.getUser();
            return variableDicService.saveOrUpdateVariableDic(vo,user,days);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }


    @ApiOperation(value = "时间控件",notes = "时间控件")
    @PostMapping("/getValidPeriod")
    public ApiResult<String> getValidPeriod(@RequestBody String startDate
            ,@RequestParam String endDate){
        String validPeriod = variableDicService.getValidPeriod(startDate,endDate);
        return new ApiResult<String>().setData(validPeriod).success();
    }


    @ApiOperation(value = "场景列表", notes = "支持apicode多选")
    @PostMapping({"/findListByCidsAndApiCodes"})
    public ApiResult<List<Map>> findListByCidsAndApiCodes(@RequestBody List<CustomerSelectVO> vos) {
        List<Map> list = variableDicService.findListByCidsAndApiCodes(vos);
        return new ApiResult<List<Map>>().success(list);
    }
    /*@ApiOperation(value = "删除客户配置变量值字典",notes = "删除客户配置变量值字典")
    @GetMapping("/delete")
    public ApiResult<Boolean> delete(Integer id){
        try {
            return variableDicService.delete(id);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }*/
}
