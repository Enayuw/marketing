package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.tccpa.TcCpDataCleanTaskDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageVO;
import com.br.marketing.dto.tccpa.TcyrCpaCollidingDataPackageVO;
import com.br.marketing.entity.TcyrCpaCollidingDataPackage;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.service.tccpa.TcCpaDataPackageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * 营销平台筛选接口
 */
@RestController
@RequestMapping("/tcCpa/customize/dataPackage")
@Tag(value = "TcCpaCustomizeController")
public class TcCpaCustomizeController {

    private static final Integer CODE_1 = 1;

    @Resource
    private TcCpaDataPackageService tcCpaDataPackageService;

    /**
     * 同程CPA跑分文件数据包删除
     * @return
     */
    @Operation(value = "同程CPA跑分文件数据包列表查询", notes = "同程CPA跑分文件数据包列表查询", httpMethod = "POST")
    @GetMapping("/page")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "packageName", value = "包名称", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "status", value = "状态", paramType = "query", dataType = "integer")
    })
    public ApiResult<PageResultReturn> page(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String packageName
            , @RequestParam(required = false) Integer status) {
        return new ApiResult<PageResultReturn>().success(tcCpaDataPackageService.page(current, size, packageName, status));
    }


    /**
     * 同程CPA跑分文件数据包新增修改
     * @param dataPackage
     * @return
     */
    @Operation(value = "同程CPA跑分文件数据包新增修改", notes = "同程CPA跑分文件数据包新增修改", httpMethod = "POST")
    @PostMapping("/update")
    public ApiResult update(@RequestBody TcyrCpaCollidingDataPackageVO dataPackage) {
        return new ApiResult().fromResult(tcCpaDataPackageService.update(dataPackage), CODE_1);
    }



    /**
     * 同程CPA跑分文件数据包删除
     * @param dto
     * @return
     */
    @Operation(value = "同程CPA跑分文件数据包删除", notes = "同程CPA跑分文件数据包删除", httpMethod = "POST")
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody TcCpDataPackageGenDTO dto) {
        return new ApiResult().fromResult(tcCpaDataPackageService.delete(dto), CODE_1);
    }

    /**
     * 同程CPA跑分文件数据包删除
     * @return
     */
    @Operation(value = "同程CPA跑分文件清洗分层任务生成", notes = "同程CPA跑分文件清洗分层任务生成", httpMethod = "POST")
    @PostMapping("/genCleanTask")
    public ApiResult genCleanTask() {
        return new ApiResult().fromResult(tcCpaDataPackageService.genCleanTask(), CODE_1);
    }

}
