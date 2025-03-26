package com.br.marketing.service.rulecenter.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.*;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.rulecenter.IRuleTaskService;
import com.br.marketing.service.rulecenter.enums.RuleCenterDataSourceEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.xiecheng.PushViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RuleTaskByScoreServiceImpl implements IRuleTaskService {

    @Resource
    ScoreXieChengServiceImpl scoreXieChengService;
    @Resource
    PushRuleService pushRuleService;

    @Override
    public Result<PushViewVO> pushPreview(PushCustomerDTO dto) {
        return getTotal(dto);
    }


    @Override
    public PushCustomerDTO buildPreviewDTO(CustomerInfoPushMain main, ScoreSearchCondition scoreSearchCondition) {
        PushCustomerDTO pushCustomerDTO = new PushCustomerDTO();
        pushCustomerDTO.setApiCode(main.getmApiCode());
        pushCustomerDTO.setBatchNumberList(Arrays.stream(main.getmCusBatchNumberList().split(","))
                .collect(Collectors.toList()));
        List<Long> collect = Arrays.stream(scoreSearchCondition.getSourceCondition().split(","))
                .map(t -> Long.valueOf(t)).collect(Collectors.toList());
        pushCustomerDTO.setFileIdList(collect);
        pushCustomerDTO.setmRuleCondition(scoreSearchCondition.getContent());
        pushCustomerDTO.setmRuleConditionShow(scoreSearchCondition.getContentShow());
        pushCustomerDTO.setmTagCondition(scoreSearchCondition.getTagContent());
        return pushCustomerDTO;
    }

    private Result<PushViewVO> getTotal(PushCustomerDTO dto) {
        int total;
        PushViewVO pushViewVO = new PushViewVO();
        if (isXieChengData(dto)) {
            total = scoreXieChengService.getXieChengDataNum(dto.getmRuleCondition(), dto.getBatchNumberList(), pushViewVO);
        } else {
            Result<PushViewVO> pushViewVOResult = pushRuleService.queryFederation(dto, pushViewVO);
            if (!ResultCode.SUCCESS.getValue().equals(pushViewVOResult.getCode())) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage(pushViewVOResult.getMessage());
            }
            total = pushViewVOResult.getData().getTotal();
        }
        if (total <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合的数据");
        }
        if (dto.getmPercentage() != null) {
            if (dto.getmPercentage().compareTo(new BigDecimal(0)) <= 0) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("百分比不能小于等于0");
            }
            Integer res = dto.getmPercentage().multiply(new BigDecimal(total)).setScale(0, RoundingMode.UP).intValue();
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
        }
        pushViewVO.setTotal(total);
        return new Result<PushViewVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushViewVO);
    }

    private  Boolean isXieChengData(PushCustomerDTO dto) {
        return scoreXieChengService.isXieCheng(dto.getApiCode(),dto.getmRuleCondition());
    }









    @Override
    public RuleCenterDataSourceEnum sourceLabel() {
        return RuleCenterDataSourceEnum.SCORE;
    }
}
