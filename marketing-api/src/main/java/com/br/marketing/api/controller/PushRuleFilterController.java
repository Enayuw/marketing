package com.br.marketing.api.controller;


import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.dto.CustomerBatchNumDTO;
import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.vo.PushInfoDetailVO;
import com.br.marketing.vo.ScoreDetailVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 策略API控制器
 *
 * @author Wang Weiwei
 * @since 2018/3/12
 */
@RestController
@RequestMapping("/pushrulefilter")
@Slf4j
public class PushRuleFilterController {

    @Autowired
    PushRuleService pushRuleService;

    @ApiOperation(value = "获取批次列表")
    @PostMapping("/getBatchInfos")
    public Result<List<ScoreDetailVo>> getBatchInfos(@RequestBody CustomerBatchNumDTO dto){
        try {
            return pushRuleService.getBatchInfos(dto);
        }catch (ParamValidErrorException ex){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
    }

    @ApiOperation(value = "获取推送列表")
    @PostMapping("/getPushInfos")
    public Result<List<PushInfoDetailVO>> getPushInfos(@RequestBody RequestPushInfoDTO dto){
        try {
            return pushRuleService.getPushInfos(dto);
        }catch (ParamValidErrorException ex){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
    }






}
