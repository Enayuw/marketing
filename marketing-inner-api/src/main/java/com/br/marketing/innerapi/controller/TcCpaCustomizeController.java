package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.tccpa.TcCpDataCleanTaskDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageVO;
import com.br.marketing.service.tccpa.TcCpaDataPackageService;
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
@RequestMapping("/tcCpa")
@Tag(value = "TcCpaCustomizeController")
public class TcCpaCustomizeController {

    private static final Integer CODE_1 = 1;

    @Resource
    private TcCpaDataPackageService tcCpaDataPackageService;

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
     * @param packageName 数据包名称
     * @param status 状态 0:禁用 1:启用
     * @return
     */
    @Operation(value = "同程CPA跑分文件数据包启用禁用", notes = "同程CPA跑分文件数据包启用禁用", httpMethod = "POST")
    @GetMapping("/enable")
    public ApiResult delete(@RequestParam("packageName") String packageName, @RequestParam("status") Integer status) {
        return new ApiResult().fromResult(tcCpaDataPackageService.enable(packageName, status), CODE_1);
    }

    /**
     * 同程CPA跑分文件数据包删除
     * @param dto
     * @return
     */
    @Operation(value = "同程CPA跑分文件清洗分层任务生成", notes = "同程CPA跑分文件清洗分层任务生成", httpMethod = "POST")
    @PostMapping("/genCleanTask")
    public ApiResult genCleanTask(@RequestBody TcCpDataCleanTaskDTO dto) {
        return new ApiResult().fromResult(tcCpaDataPackageService.genCleanTask(dto), CODE_1);
    }

}
