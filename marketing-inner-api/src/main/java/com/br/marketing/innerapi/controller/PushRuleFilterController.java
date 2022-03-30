package com.br.marketing.innerapi.controller;

import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CustomerBatchNumDTO;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.innerapi.config.ThreadContextInfo;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.vo.PushInfoDetailVO;
import com.br.marketing.vo.ScoreDetailVo;
import io.swagger.annotations.Api;
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

    @Autowired
    RabbitMqProducter producter;

    @Autowired
    PushRuleService pushRuleService;


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
        return new ApiResult().fromResult(companyAndModule, 000000);
    }


    /**
     * 获取批次列表
     *
     * @param dto
     * @return
     */
    @ApiOperation(value = "获取批次列表")
    @PostMapping("/getBatchInfos")
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
    public ApiResult<Integer> getBatchInfosCounts(@RequestBody CustomerBatchNumDTO dto) {
        Integer totalNum = pushRuleService.getBatchInfosCounts(dto);
        return new ApiResult<Integer>().success(totalNum);
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
        return new ApiResult<List<PushInfoDetailVO>>().fromResult(pushRuleService.getPushInfos(dto), 1);
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
        return new ApiResult().fromResult(pushRuleService.pushCustomer(dto), 1);
    }

    @ApiOperation(value = "推送预览")
    @PostMapping("/pushPreview")
    public ApiResult<Integer> pushPreview(@RequestBody PushCustomerDTO dto) {
        return new ApiResult<Integer>().fromResult(pushRuleService.pushPreview(dto), 1);
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


}
