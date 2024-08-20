package com.br.marketing.service.rulecenter.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.mapper.XieChengRuleScoreRecordMapper;
import com.br.marketing.mapper.XiechengCollidingDataPackageRuleMapper;
import com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapper;
import com.br.marketing.service.rulecenter.IRuleTaskService;
import com.br.marketing.service.rulecenter.enums.RuleCenterDataSourceEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.br.marketing.util.xiecheng.XieChengEsJsonHandler;
import com.br.marketing.vo.xiecheng.PushViewVO;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

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
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    MarketingHistoryEsService marketingHistoryEsService;



    @Resource
    ScoreXieChengServiceImpl scoreXieChengService;

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
        return pushCustomerDTO;
    }

    private Result<PushViewVO> getTotal(PushCustomerDTO dto) {
        int total;
        PushViewVO pushViewVO = new PushViewVO();
        if (isXieChengData(dto)) {
            total = scoreXieChengService.getXieChengDataNum(dto.getmRuleCondition(), dto.getBatchNumberList(), pushViewVO);
        } else {
            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(dto.getApiCode());
            queryBaseBean.setBatchNumbers(Joiner.on(",").join(dto.getBatchNumberList()));
            queryBaseBean.setFileIds(Joiner.on(",").join(dto.getFileIdList()));
            queryBaseBean.setJsonData(dto.getmRuleCondition());
            if (dto.getmPlanNum() != null && dto.getmPlanNum() <= 0) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送数量不能小于等于0");
            }
            if (dto.getmPlanNum() != null && dto.getmPlanNum() > 0) {
                queryBaseBean.setAmountTop("0,".concat(dto.getmPlanNum().toString()));
            }
            total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
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
