package com.br.marketing.innerapi.controller;

import com.br.marketing.aspect.AuthDataControllerPermission;
import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.aspect.LogRecordAnnotation;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.*;
import com.br.marketing.dto.tccpa.TcCpDataCleanTaskDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.entity.TcyrCpaCollidingDataCleanTask;
import com.br.marketing.enums.InterfaceOperationsEnum;
import com.br.marketing.innerapi.service.RuleCenterCollidingService;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.Impl.RuleCenterServiceImpl;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.ReportScoreRuleService;
import com.br.marketing.service.datagroup.rulecenter.RuleCenterLabelService;
import com.br.marketing.service.halo.HaloRuleCenterCallbackService;
import com.br.marketing.service.tccpa.TcCpaDataPackageService;
import com.br.marketing.vo.*;
import com.br.marketing.vo.xiecheng.PushViewVO;
import com.br.marketing.vo.xiecheng.XiechengCollidingDataVO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 营销平台筛选接口
 */
@RestController
@RequestMapping("/tcCpa")
@Tag(value = "TcCpaCustomizeController")
public class TcCpaCustomizeController {

    private static final Integer CODE_1 = Integer.valueOf(1);

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
