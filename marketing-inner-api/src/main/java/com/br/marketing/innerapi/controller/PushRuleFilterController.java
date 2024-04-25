package com.br.marketing.innerapi.controller;

import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.aspect.LogRecordAnnotation;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.*;
import com.br.marketing.dto.CustomerBatchNumDTO;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.enums.InterfaceOperationsEnum;
import com.br.marketing.innerapi.service.RuleCenterCollidingService;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.Impl.RuleCenterServiceImpl;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.vo.ConditionOfScoreVO;
import com.br.marketing.vo.PushInfoDetailVO;
import com.br.marketing.vo.ScoreConditionDetailVO;
import com.br.marketing.vo.xiecheng.XiechengCollidingDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 营销平台筛选接口
 */
@RestController
@RequestMapping("/pushrulefilter")
@Api(value = "PushRuleFilterController")
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
    RabbitMqProducter producter;

    @Autowired
    PushRuleService pushRuleService;

    @Autowired
    RuleCenterServiceImpl ruleCenterService;

    @Autowired
    RuleCenterCollidingService ruleCenterCollidingService;



    /**
     * 根据apiCode 查询信息
     *
     * @param apiCode
     * @return
     */
    @ApiOperation(value = "根据apiCode 查询信息")
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
    @ApiOperation(value = "根据apiCode 查询信息")
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
    @ApiOperation(value = "获取批次列表")
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
    @ApiOperation(value = "获取列表跑分总数")
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
    @ApiOperation(value = "获取推送列表")
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
    @ApiOperation(value = "推送客服")
    @PostMapping("/pushCustomer")
    public ApiResult pushCustomer(@RequestBody PushCustomerDTO dto) {
        dto.setUserDetail(ThreadContextInfo.getUser());
        return new ApiResult().fromResult(ruleCenterService.pushCustomer(dto), CODE_1);
    }

    @ApiOperation(value = "推送预览")
    @PostMapping("/pushPreview")
    public ApiResult<Integer> pushPreview(@RequestBody PushCustomerDTO dto) {
        return new ApiResult<Integer>().fromResult(pushRuleService.pushPreview(dto), CODE_1);
    }

    @ApiOperation(value = "保存模板")
    @PostMapping("/saveCondition")
    public ApiResult<Long> saveCondition(@RequestBody ConditionSaveDTO dto) {
        return new ApiResult<Long>().fromResult(pushRuleService.saveCondition(dto), CODE_1);
    }

    @ApiOperation(value = "获取模板")
    @GetMapping("/getConditionByRule")
    public ApiResult<List<ConditionOfScoreVO>> getConditionByRule(String apiCode,String name) {
        return new ApiResult<List<ConditionOfScoreVO>>().fromResult(pushRuleService.getConditionByRule(apiCode,name), CODE_1);
    }

    @ApiOperation(value = "修改规则模板")
    @PostMapping("/optCondition")
    public ApiResult optCondition(@RequestBody OptConditionDTO dto) {
        return new ApiResult().fromResult(pushRuleService.optCondition(dto), CODE_1);
    }

    @ApiOperation(value = "查询规则模板列表")
    @PostMapping("/getConditionPageData")
    public ApiResult<PageResultReturn<ScoreConditionDetailVO>> getConditionPageData(@RequestBody SearchConditionDTO dto) {
        return new ApiResult<PageResultReturn<ScoreConditionDetailVO>>().fromResult(pushRuleService.getConditionPageData(dto), CODE_1);
    }

    /**
     * 根据apiCode 查询撞库结果数据
     * @param apiCode
     * @return
     */
    @ApiOperation(value = "撞库结果数据", notes = "撞库结果数据", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "apiCode", value = "apiCode", paramType = "query", dataType = "string")})
    @GetMapping("/getCollidingResultData")
    public ApiResult getCollidingResultData(String apiCode) {
        return new ApiResult<List<XiechengCollidingDataVO>>().fromResult(ruleCenterCollidingService.getCollidingResultData(apiCode), CODE_1);
    }


    /**
     * 撞库数据剔除
     * @param dto
     * @return
     */
    @ApiOperation(value = "撞库数据剔除", notes = "撞库数据剔除", httpMethod = "POST")
    @PostMapping("/collidingDataDelete")
    public ApiResult collidingDataDelete(@RequestBody PushCustomerDTO dto) {
        return new ApiResult().fromResult(pushRuleService.collidingDataDelete(dto), CODE_1);
    }

    /**
     * 撞库数据剔除数据量
     * @param dto
     * @return
     */
    @ApiOperation(value = "撞库数据剔除数据量", notes = "撞库数据剔除数据量", httpMethod = "GET")
    @GetMapping("/collidingDataDeleteNum")
    public ApiResult collidingDataDeleteNum(@RequestBody PushCustomerDTO dto) {
        return new ApiResult<Integer>().fromResult(pushRuleService.collidingDataDeleteNum(dto), CODE_1);
    }

    /**
     * 撞库数据包生成
     * @param dto
     * @return
     */
    @ApiOperation(value = "撞库数据包生成", notes = "撞库数据包生成", httpMethod = "POST")
    @PostMapping("/collidingDataPachageMake")
    public ApiResult collidingDataPachageMake(@RequestBody PushCustomerDTO dto) {
        return new ApiResult().fromResult(pushRuleService.collidingDataPachageMake(dto), CODE_1);
    }

    @ApiOperation(value = "测试消费")
    @GetMapping("/testConsumerCustomer")
    public Result testConsumerCustomer(Long id) {
        return pushRuleService.consumerPushCustomer(id);
    }

    /**
     * 测试MQ
     *
     * @return
     */
    @ApiOperation(value = "测试rabbit")
    @PostMapping("/testRabbitProduct")
    public String testRabbitProduct() {
        producter.send("hehe", "还有谁");
//        producter.send("hehe",12L);
//        producter.send("hehe",String.valueOf(12L));
        return "true";
    }

    /**
     * 测试通用日志
     */
    @ApiOperation(value = "测试通用日志")
    @GetMapping("/testLog")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.XIECHENG_INSERT_DATA,
            extendInfo= "修改了数据包一中的原开启撞库时间{#dto.apiCode}的设定撞得量级[getUserName{#dto.cell}]修改为{#dto.cell}的设定撞得量级{#dto.dataCode}")
    //@LogRecordAnnotation(bizNo = InterfaceOperationsEnum.XIECHENG_INSERT_DATA,
    //        extendInfo= "#dto.custNum == null ? '新增' + #dto.custNum + '用户':'将用户id为' + #dto.custNum + '的用户名更新为' + #dto.custNum")
    public ApiResult testLog(@RequestBody DataJoinLogDTO dto) {
        Result<Map<String, Object>> companyAndModule = pushRuleService.getCompanyAndModule("7491630");
        return new ApiResult().fromResult(companyAndModule, CODE_000000);
    }

}
