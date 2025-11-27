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
import java.util.Map;

/**
 * 客户配置变量值字典
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 17:40
 */
@RestController
@RequestMapping(value = "/rule/vd")
@Tag(name = "客户配置变量值", description = "客户配置变量值字典")
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
    @Operation(summary = "配置变量值字典", description = "集合")
    @Parameters({@Parameter(name = "cid", description = "合作客户id")
            , @Parameter(name = "apiCode", description = "接口编号")
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    @GetMapping({"/list/{cid}/{apiCode}"})
    public ApiResult<List<VariableDicSelectVO>> findListByCidAndApiCode(@PathVariable(value = "cid") String cid
            , @PathVariable(value = "apiCode") String apiCode) {
        List<VariableDicSelectVO> list = variableDicService.findListByCidAndApiCode(cid, apiCode);
        return new ApiResult<List<VariableDicSelectVO>>().setData(list).success();
    }


    @GetMapping("/getVariableDicList")
    @Operation(summary = "客户配置变量值列表数据", description = "客户配置变量值列表数据")
    @Parameters({@Parameter(name = "current", description = "页号")
            , @Parameter(name = "size", description = "页大小")
            , @Parameter(name = "cid", description = "合作客户id")
            , @Parameter(name = "apiCode", description = "接口编号")
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


    @Operation(summary = "新增/变更客户配置变量值字典", description = "新增/变更客户配置变量值字典")
    @PostMapping("/saveOrUpdateVariableDic")
    public ApiResult<Boolean> saveOrUpdateVariableDic(@RequestBody @Validated VariableDicListVO vo){
        try {
            //获取用户上下文
            MarketingUserDetail user = ThreadContextInfo.getUser();
            return variableDicService.saveOrUpdateVariableDic(vo,user);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }



    @Operation(summary = "场景列表", description = "支持apicode多选")
    @PostMapping({"/findListByCidsAndApiCodes"})
    public ApiResult<List<Map>> findListByCidsAndApiCodes(@RequestBody List<CustomerSelectVO> vos) {
        List<Map> list = variableDicService.findListByCidsAndApiCodes(vos);
        return new ApiResult<List<Map>>().success(list);
    }
    /*@Operation(summary = "删除客户配置变量值字典", description = "删除客户配置变量值字典")
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
