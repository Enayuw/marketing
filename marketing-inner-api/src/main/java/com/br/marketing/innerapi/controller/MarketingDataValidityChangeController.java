package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.innerapi.service.impl.TaskOptServiceImpl;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.MarketingDataValidityChangeService;
import com.br.marketing.service.MarketingTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * -------------------------------
 *
 * @author guangxiu.li
 * @Description 有效期更改
 * @Date 2023/09/26 10:19 AM
 * ------------------------------
 */

@RestController
@RequestMapping("/validity/task")
@Slf4j
@Api(value = "有效期更改", tags = "有效期更改", produces = "application/json", consumes = "application/json", protocols = "http")
public class MarketingDataValidityChangeController {

    @Autowired
    MarketingDataValidityChangeService marketingDataValidityChangeService;

    @Autowired
    MarketingTaskService marketingTaskService;

    @Autowired
    TaskOptServiceImpl taskOptService;

    @ApiOperation(value = "有效期记录列表", notes = "有效期记录列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "Integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "Integer", defaultValue = "10")
            , @ApiImplicitParam(name = "apiCode", value = "apiCode", paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "isDel", value = "是否有效 1-有效 9-无效", paramType = "query", dataType = "Integer")
            , @ApiImplicitParam(name = "userType", value = "场景", paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "validStartDate", value = "生效开始日期", paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "validEndDate", value = "生效结束日期", paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "validDays", value = "T+N 有效期方式配置", paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "validType", value = "1-有效期范围配置；2-有效期T+N配置；3-有效期T+N配置与场景（userType）", paramType = "query", dataType = "Integer")
            , @ApiImplicitParam(name = "appletDate", value = "上传日期", paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "createTime", value = "创建时间", paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "updateTime", value = "修改时间", paramType = "query", dataType = "String")
    })
    @GetMapping("/validityList")
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> validityList(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String apiCode
            , @RequestParam(defaultValue = "1") Integer isDel
            , @RequestParam(required = false) String userType
            , @RequestParam(required = false) String validStartDate
            , @RequestParam(required = false) String validEndDate
            , @RequestParam(required = false) String validDays
            , @RequestParam(required = false) Integer validType
            , @RequestParam(required = false) String appletDate
            , @RequestParam(required = false) String createTime
            , @RequestParam(required = false) String updateTime
    ) {
        PageResultReturn list = marketingDataValidityChangeService.list(current, size, apiCode, isDel, userType,
                validStartDate, validEndDate ,validDays ,validType, appletDate, createTime ,updateTime);
        return new ApiResult<PageResultReturn>().success(list);
    }


    @ApiOperation(value = "新增有效期记录", notes = "新增有效期记录")
    @GetMapping("/saveValidity")
    @AddDataAuthBusiness
    public ApiResult<Boolean> saveValidity(MarketingDataValidConfig marketingDataValidConfig) {
        try {
            boolean flag = marketingDataValidityChangeService.save(marketingDataValidConfig);
            if (flag) {
                return new ApiResult<Boolean>().success(true, "新增有效期记录成功！");
            } else {
                return new ApiResult<Boolean>().success(false, "新增有效期记录失败！");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }


    @ApiOperation(value = "删除有效期记录", notes = "删除有效期记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id", required = true, dataType = "Long")})
    @GetMapping("/delValidity")
    public ApiResult delValidity(@RequestParam Long id) {
        return new ApiResult().fromResult(marketingDataValidityChangeService.delTask(id), 1);
    }

    @ApiOperation(value = "修改有效期记录", notes = "修改有效期记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id", required = true, dataType = "Long")
            , @ApiImplicitParam(name = "validStartDate", value = "生效开始日期", required = true, paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "validEndDate", value = "生效结束日期", required = true, paramType = "query", dataType = "String")
            , @ApiImplicitParam(name = "resendType", value = "是否需要重推", paramType = "query", dataType = "Integer")
    })
    @GetMapping("/updateValidity")
    public ApiResult<Boolean> updateValidity(@RequestParam Long id
            , @RequestParam String validStartDate
            , @RequestParam String validEndDate
            , @RequestParam(defaultValue = "0") Integer resendType) {
        //查询
        try {
            if (resendType != 0) {
                //开始重推
                log.warn("重推编号："+ resendType);



            }
            boolean flag = marketingDataValidityChangeService.updateById(id, validStartDate, validEndDate);
            if (flag) {
                return new ApiResult<Boolean>().success(true, "操作成功！");
            } else {
                return new ApiResult<Boolean>().success(false, "操作失败！");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }


}
