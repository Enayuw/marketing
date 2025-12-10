package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tccpa.TcyrCpaCollidingDataPackageVO;
import com.br.marketing.service.tccpa.TcCpaDataPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * 营销平台筛选接口
 */
@RestController
@RequestMapping("/tcCpa/customize/dataPackage")
@Tag(name = "同程CPA数据包管理", description = "同程CPA跑分文件数据包相关接口")
public class TcCpaCustomizeController {

    private static final Integer CODE_1 = 1;

    @Resource
    private TcCpaDataPackageService tcCpaDataPackageService;

    /**
     * 同程CPA跑分文件数据包列表查询
     * @return
     */
    @Operation(summary = "同程CPA跑分文件数据包列表查询", description = "同程CPA跑分文件数据包列表查询")
    @GetMapping("/page")
    @Parameters({
            @Parameter(name = "current", description = "页号"),
            @Parameter(name = "size", description = "页大小"),
            @Parameter(name = "packageName", description = "包名称"),
            @Parameter(name = "status", description = "状态")
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
    @Operation(summary = "同程CPA跑分文件数据包新增修改", description = "同程CPA跑分文件数据包新增修改")
    @PostMapping("/update")
    public ApiResult update(@RequestBody TcyrCpaCollidingDataPackageVO dataPackage) {
        return new ApiResult().fromResult(tcCpaDataPackageService.update(dataPackage), CODE_1);
    }



    /**
     * 同程CPA跑分文件数据包删除
     * @return
     */
    @Operation(summary = "同程CPA跑分文件数据包删除", description = "同程CPA跑分文件数据包删除")
    @GetMapping("/delete")
    @Parameter(name = "id", description = "数据包ID")
    public ApiResult delete(@RequestParam("id") Long id) {
        return new ApiResult().fromResult(tcCpaDataPackageService.delete(id), CODE_1);
    }

    /**
     * 同程CPA跑分文件清洗分层任务生成
     * @return
     */
    @Operation(summary = "同程CPA跑分文件清洗分层任务生成", description = "同程CPA跑分文件清洗分层任务生成")
    @PostMapping("/genCleanTask")
    public ApiResult genCleanTask() {
        return new ApiResult().fromResult(tcCpaDataPackageService.genCleanTask(), CODE_1);
    }

}
