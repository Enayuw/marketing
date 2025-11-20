package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.tccpa.TcCpDataCleanTaskDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageVO;
import com.br.marketing.service.tccpa.TcCpaDataPackageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * 营销平台筛选接口
 */
@RestController
@RequestMapping("/tcCpa/customize/dataPackage")
@Api(value = "TcCpaCustomizeController")
public class TcCpaCustomizeController {

    private static final Integer CODE_1 = 1;

    @Resource
    private TcCpaDataPackageService tcCpaDataPackageService;

    /**
     * 同程CPA跑分文件数据包删除
     * @param dto
     * @return
     */
    @ApiOperation(value = "同程CPA跑分文件数据包删除", notes = "同程CPA跑分文件数据包删除", httpMethod = "POST")
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody TcCpDataPackageGenDTO dto) {
        return new ApiResult().fromResult(tcCpaDataPackageService.delete(dto), CODE_1);
    }

    /**
     * 同程CPA跑分文件数据包删除
     * @param packageVO 数据包
     * @return
     */
    @ApiOperation(value = "同程CPA跑分文件数据包启用禁用", notes = "同程CPA跑分文件数据包启用禁用", httpMethod = "POST")
    @PostMapping("/enable")
    public ApiResult enable(@RequestBody TcCpDataPackageVO packageVO) {
        return new ApiResult().fromResult(tcCpaDataPackageService.enable(packageVO.getPackageName(), packageVO.getStatus()), CODE_1);
    }

    /**
     * 同程CPA跑分文件数据包删除
     * @param dto
     * @return
     */
    @ApiOperation(value = "同程CPA跑分文件清洗分层任务生成", notes = "同程CPA跑分文件清洗分层任务生成", httpMethod = "POST")
    @PostMapping("/genCleanTask")
    public ApiResult genCleanTask(@RequestBody TcCpDataCleanTaskDTO dto) {
        return new ApiResult().fromResult(tcCpaDataPackageService.genCleanTask(dto), CODE_1);
    }

}
