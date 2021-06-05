package com.br.marketing.api.controller;
import java.util.Date;


import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.dto.CustomerBatchNumDTO;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.dto.RequestPushInfoDTO;
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

import java.util.ArrayList;
import java.util.List;


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
     * 获取批次列表
     * @param dto
     * @return
     */
    @ApiOperation(value = "获取批次列表")
    @PostMapping("/getBatchInfos")
    public Result<List<ScoreDetailVo>> getBatchInfos(@RequestBody CustomerBatchNumDTO dto){
        try {
            return pushRuleService.getBatchInfos(dto);
        }catch (ParamValidErrorException ex){
            log.error(ex.getMessage());
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
    }

    /**
     * 获取推送列表
     * @param dto
     * @return
     */
    @ApiOperation(value = "获取推送列表")
    @PostMapping("/getPushInfos")
    public Result<List<PushInfoDetailVO>> getPushInfos(@RequestBody RequestPushInfoDTO dto){
        try {
            return pushRuleService.getPushInfos(dto);
        }catch (ParamValidErrorException ex){
            log.error(ex.getMessage());
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
    }

    /**
     * 推送客服
     * @param dto
     * @return
     */
    @ApiOperation(value = "推送客服")
    @PostMapping("/pushCustomer")
    public Result pushCustomer(@RequestBody PushCustomerDTO dto){
        try {
            return pushRuleService.pushCustomer(dto);
        }catch (ParamValidErrorException ex){
            log.error(ex.getMessage());
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
    }

    /**
     * 测试MQ
     * @return
     */
    @ApiOperation(value = "测试rabbit")
    @PostMapping("/testRabbitProduct")
    public String testRabbitProduct(){
        producter.send("hehe","还有谁");
//        producter.send("hehe",12L);
//        producter.send("hehe",String.valueOf(12L));
        return "true";
    }


}
