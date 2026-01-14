package com.br.marketing.innerapi.controller;

import com.br.marketing.aspect.AuthDataControllerPermission;
import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.aspect.LogRecordAnnotation;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.*;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.dto.rulecenter.XcCycleDeleteDTO;
import com.br.marketing.dto.rulecenter.XcCycleDeleteNumDTO;
import com.br.marketing.dto.rulecenter.XcDeleteMagnitudeDistDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 营销平台筛选接口
 */
@RestController
@RequestMapping("/pushrulefilter")
@Tag(name = "PushRuleFilterController", description = "PushRuleFilterController")
public class PushRuleFilterController {

    private static final Logger log = LoggerFactory.getLogger(PushRuleFilterController.class);
    /**
     * CODE_000000
     */
    private static final Integer CODE_000000 = Integer.valueOf("000000");
    /**
     * CODE_1
     */
    private static final Integer CODE_1 = Integer.valueOf(1);


    @Autowired
    PushRuleService pushRuleService;

    @Autowired
    RuleCenterServiceImpl ruleCenterService;

    @Autowired
    RuleCenterCollidingService ruleCenterCollidingService;

    @Resource
    private ReportScoreRuleService reportScoreRuleService;

    @Autowired
    RuleCenterLabelService ruleCenterLabelService;

    @Autowired
    HaloRuleCenterCallbackService haloRuleCenterCallbackService;

    @Resource
    TcCpaDataPackageService tcCpaDataPackageService;


    /**
     * 根据apiCode 查询信息
     *
     * @param apiCode
     * @return
     */
    @Operation(summary = "根据apiCode 查询信息")
    @GetMapping("/getCompanyAndModule")
    @LogAnnotation
    public ApiResult getCompanyAndModule(String apiCode) {
        Result<Map<String, Object>> companyAndModule = pushRuleService.getCompanyAndModule(apiCode);
        return new ApiResult().fromResult(companyAndModule, CODE_000000);
    }

    /**
     * 根据apiCode 查询信息
     * @param apiCode
     * @return
     */
    @Operation(summary = "根据apiCode 查询信息")
    @GetMapping("/getUserType")
    @LogAnnotation
    public ApiResult getUserType(String apiCode) {
        Result<String> userType = pushRuleService.getUserType(apiCode);
        return new ApiResult().fromResult(userType, CODE_000000);
    }


    /**
     * 获取批次列表
     *
     * @param dto
     * @return
     */
    @Operation(summary = "获取批次列表")
    @PostMapping("/getBatchInfos")
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getBatchInfos(@RequestBody CustomerBatchNumDTO dto) {
        PageResultReturn listPage = pushRuleService.getBatchInfos(dto);
        return new ApiResult<PageResultReturn>().success(listPage);
    }

    /**
     * 获取列表跑分总数
     *
     * @param dto
     * @return
     */
    @Operation(summary = "获取列表跑分总数")
    @PostMapping("/getBatchInfosCounts")
    @AddDataAuthBusiness
    public ApiResult<Long> getBatchInfosCounts(@RequestBody CustomerBatchNumDTO dto) {
        Long totalNum = pushRuleService.getBatchInfosCounts(dto);
        return new ApiResult<Long>().success(totalNum);
    }

    /**
     * 获取推送列表
     *
     * @param dto
     * @return
     */
    @Operation(summary = "获取推送列表")
    @PostMapping("/getPushInfos")
    public ApiResult<List<PushInfoDetailVO>> getPushInfos(@RequestBody RequestPushInfoDTO dto) {
        return new ApiResult<List<PushInfoDetailVO>>().fromResult(pushRuleService.getPushInfos(dto), CODE_1);
    }

    /**
     * 推送客服
     *
     * @param dto
     * @return
     */
    @Operation(summary = "推送客服")
    @PostMapping("/pushCustomer")
    public ApiResult pushCustomer(@RequestBody PushCustomerDTO dto) {
        dto.setUserDetail(ThreadContextInfo.getUser());
        return new ApiResult().fromResult(ruleCenterService.pushCustomer(dto), CODE_1);
    }

    @Operation(summary = "推送预览")
    @PostMapping("/pushPreview")
    public ApiResult<PushViewVO> pushPreview(@RequestBody PushCustomerDTO dto) {
        return new ApiResult<PushViewVO>().fromResult(pushRuleService.pushPreview(dto), CODE_1);
    }

    @Operation(summary = "保存模板")
    @PostMapping("/saveCondition")
    public ApiResult<Long> saveCondition(@RequestBody ConditionSaveDTO dto) {
        return new ApiResult<Long>().fromResult(pushRuleService.saveCondition(dto), CODE_1);
    }

    @Operation(summary = "删除规则模板")
    @GetMapping("/deleteRule")
    public ApiResult<Boolean> deleteRule(@RequestParam Long id) {
        return new ApiResult<Boolean>().fromResult(pushRuleService.deleteRule(id), CODE_1);
    }

    @Operation(summary = "获取模板")
    @GetMapping("/getConditionByRule")
    public ApiResult<List<ConditionOfScoreVO>> getConditionByRule(String apiCode,String name) {
        return new ApiResult<List<ConditionOfScoreVO>>().fromResult(pushRuleService.getConditionByRule(apiCode,name), CODE_1);
    }

    @Operation(summary = "获取规则模板列表")
    @GetMapping("/getConditionList")
    public ApiResult<List<ConditionVO>> getConditionList(String apiCode,String content) {
        return new ApiResult<List<ConditionVO>>().fromResult(pushRuleService.getConditionList(apiCode,content), CODE_1);
    }

    @Operation(summary = "根据apiCode和规则模板id获取规则模板详情")
    @GetMapping("/getConditionById")
    public ApiResult<ConditionVO> getConditionById(String apiCode,Long conditionId) {
        return new ApiResult<ConditionVO>().fromResult(pushRuleService.getConditionById(apiCode,conditionId), CODE_1);
    }

    @Operation(summary = "修改规则模板")
    @PostMapping("/optCondition")
    public ApiResult optCondition(@RequestBody OptConditionDTO dto) {
        return new ApiResult().fromResult(pushRuleService.optCondition(dto), CODE_1);
    }

    @Operation(summary = "查询规则模板列表")
    @PostMapping("/getConditionPageData")
    public ApiResult<PageResultReturn<ScoreConditionDetailVO>> getConditionPageData(@RequestBody SearchConditionDTO dto) {
        return new ApiResult<PageResultReturn<ScoreConditionDetailVO>>().fromResult(pushRuleService.getConditionPageData(dto), CODE_1);
    }

    /**
     * 根据apiCode 查询撞库结果数据
     * @param apiCode
     * @return
     */
    @Operation(summary = "撞库结果数据", description = "撞库结果数据")
    @Parameters({@Parameter(name = "apiCode", description = "apiCode")})
    @GetMapping("/getCollidingResultData")
    public ApiResult getCollidingResultData(String apiCode) {
        return new ApiResult<List<XiechengCollidingDataVO>>().fromResult(ruleCenterCollidingService.getCollidingResultData(apiCode), CODE_1);
    }


    /**
     * 撞库数据剔除，非周期数据动态补充包剔除、黑名单剔除
     * @param dto
     * @return
     */
    @Operation(summary = "撞库数据剔除", description = "撞库数据剔除")
    @PostMapping("/collidingDataDelete")
    public ApiResult collidingDataDelete(@RequestBody PushCustomerDTO dto) {
        return new ApiResult().fromResult(pushRuleService.collidingDataDelete(dto), CODE_1);
    }

    @Operation(summary = "撞库周期数据剔除", description = "撞库周期数据剔除")
    @PostMapping("/collidingDataCycleDelete")
    public ApiResult collidingDataCycleDelete(@RequestBody @Valid XcCycleDeleteDTO dto) {
        return new ApiResult().fromResult(pushRuleService.collidingDataCycleDelete(dto), CODE_1);
    }

    /**
     * 撞库数据剔除数据量
     * @param dto
     * @return
     */
    @Operation(summary = "撞库数据剔除数据量", description = "撞库数据剔除数据量")
    @PostMapping("/collidingDataDeleteNum")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.XIECHENG_DELETE_COLLIDING_PACKAGE,
            extendInfo = "使用{#dto.mRuleCondition}，进行数据量级{#dto.mPlanNum}的数据剔除")
    public ApiResult collidingDataDeleteNum(@RequestBody PushCustomerDTO dto) {
        return new ApiResult<Integer>().fromResult(pushRuleService.collidingDataDeleteNum(dto), CODE_1);
    }

    @Operation(summary = "撞库数据周期剔除量级分布", description = "撞库数据周期剔除量级分布")
    @PostMapping("/collidingDataCycleDeleteMagnitudeDist")
    public ApiResult collidingDataCycleDeleteMagnitudeDist(@RequestBody @Valid XcCycleDeleteNumDTO dto) {
        return new ApiResult<List<XcDeleteMagnitudeDistDTO>>()
                .fromResult(pushRuleService.collidingDataCycleDeleteMagnitudeDist(dto), CODE_1);
    }

    /**
     * 撞库数据包生成
     * @param dto
     * @return
     */
    @Operation(summary = "撞库数据包生成", description = "撞库数据包生成")
    @PostMapping("/collidingDataPachageMake")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.XIECHENG_MAKE_COLLIDING_PACKAGE,
            extendInfo = "使用{#dto.mRuleCondition}，生成数据量级{#dto.mPrePlanNum}的{#dto.dataPackageName}数据包")
    public ApiResult collidingDataPachageMake(@RequestBody PushCustomerDTO dto) {
        return new ApiResult().fromResult(pushRuleService.collidingDataPachageMake(dto), CODE_1);
    }

    @Operation(summary = "测试消费")
    @GetMapping("/testConsumerCustomer")
    public Result testConsumerCustomer(Long id) {
        return pushRuleService.consumerPushCustomer(id);
    }


    /**
     * 测试通用日志
     */
    @Operation(summary = "测试通用日志")
    @GetMapping("/testLog")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.XIECHENG_INSERT_DATA,
            extendInfo = "修改了数据包一中的原开启撞库时间{#dto.apiCode}的设定撞得量级[getUserName{#dto.cell}]修改为{#dto.cell}的设定撞得量级{#dto.dataCode}")
    //@LogRecordAnnotation(bizNo = InterfaceOperationsEnum.XIECHENG_INSERT_DATA,
    //        extendInfo= "#dto.custNum == null ? '新增' + #dto.custNum + '用户':'将用户id为' + #dto.custNum + '的用户名更新为' + #dto.custNum")
    public ApiResult testLog(@RequestBody DataJoinLogDTO dto) {
        Result<Map<String, Object>> companyAndModule = pushRuleService.getCompanyAndModule("7491630");
        return new ApiResult().fromResult(companyAndModule, CODE_000000);
    }


    /**
     * 跑分模型分布筛选批次列表
     * 规则中心筛选批次列表（评分产品析出字段）
     *
     * @param batchNumVO 检索条件
     * @author Hua Qiang
     * @date 2024-08-15 17:07
     */
    @Operation(summary = "跑分模型分布筛选批次列表")
    @PostMapping("getBatchInfoList")
    @AuthDataControllerPermission
    public ApiResult<PageResultReturn<List<ScoreDetailVo>>> getBatchInfoList(@RequestBody CustomerBatchNumVO batchNumVO) {
        return new ApiResult<PageResultReturn<List<ScoreDetailVo>>>().success(reportScoreRuleService.getBatchInfoList(batchNumVO));
    }


    /**
     * 获取分组字段列表
     *
     * @param apiCode
     * @return
     */
    @Operation(summary = "获取分组字段列表", description = "获取分组字段列表")
    @Parameters({@Parameter(name = "apiCode", description = "apiCode")})
    @GetMapping("/getLableNameList")
    public ApiResult getLableNameList(@RequestParam(required = false) String apiCode) {
        return new ApiResult<Set<String>>().fromResult(ruleCenterLabelService.getLabelNames(apiCode), CODE_1);
    }

    /**
     * 保存分组任务
     * @param dto
     * @return
     */
    @Operation(summary = "保存分组任务")
    @PostMapping("/saveLabelTask")
    public ApiResult saveLabelTask(@RequestBody PushCustomerDTO dto) {
        dto.setUserDetail(ThreadContextInfo.getUser());
        return new ApiResult().fromResult(ruleCenterLabelService.saveLabelTask(dto), CODE_1);
    }


    /**
     * 获取跑分合并标识
     *
     * @param batchNumbers
     * @return
     */
    @Operation(summary = "获取跑分合并标识", description = "获取跑分合并标识")
    @Parameters({@Parameter(name = "batchNumbers", description = "跑分批次号，多个用,分割"),
            @Parameter(name = "apiCode", description = "apiCode")})
    @GetMapping("/getScoreMergeMark")
    public ApiResult getScoreMergeMark(@RequestParam String batchNumbers,@RequestParam String apiCode) {
        return new ApiResult<Boolean>().fromResult(ruleCenterLabelService.getScoreMergeMark(batchNumbers,apiCode), CODE_1);
    }


    /**
     * 获取跑分合并量级
     *
     * @param batchNumbers
     * @return
     */
    @Operation(summary = "获取跑分合并量级", description = "获取跑分合并量级")
    @Parameters({@Parameter(name = "batchNumbers", description = "跑分批次号，多个用,分割"),
            @Parameter(name = "apiCode", description = "apiCode")})
    @GetMapping("/getScoreMergeNum")
    public ApiResult getScoreMergeNum(@RequestParam String batchNumbers,@RequestParam String apiCode) {
        return new ApiResult<Map<String,Integer>>().fromResult(ruleCenterLabelService.getScoreMergeNum(batchNumbers,apiCode), CODE_1);
    }

    /**
     * 生成哈啰硅基人回调任务
     * @param dto
     * @return
     */
    @Operation(summary = "生成哈啰硅基人回调任务")
    @PostMapping("/saveHaloCallbackTask")
    public ApiResult saveHaloCallbackTask(@RequestBody PushCustomerDTO dto){
        dto.setUserDetail(ThreadContextInfo.getUser());
        return new ApiResult().fromResult(haloRuleCenterCallbackService.saveHaloCallbackTask(dto),CODE_1);
    }

    /**
     * 校验apiCode是否可推送客户系统
     */
    @Operation(summary = "校验apiCode是否可推送客户系统")
    @PostMapping("/canPushCallback")
    public ApiResult canPushCallback(@RequestParam("apiCode") String apiCode){
        return new ApiResult().fromResult(haloRuleCenterCallbackService.canPushCallback(apiCode),CODE_1);
    }

    /**
     * 同程CPA跑分待清洗数据包生成
     * @param dto
     * @return
     */
    @Operation(summary = "同程CPA跑分待清洗数据包生成")
    @PostMapping("/tcCpaDataPackageGen")
    public ApiResult tcDataPackageGen(@RequestBody @Valid TcCpDataPackageGenDTO dto) {
        return new ApiResult().fromResult(tcCpaDataPackageService.tcDataPackageGen(dto), CODE_1);
    }

}
