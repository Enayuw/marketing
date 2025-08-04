package com.br.marketing.service.datagroup.rulecenter.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.CustomerInfoPushMainExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.enums.rulecenter.RuleCenterPushTargetEnum;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.datagroup.rulecenter.RuleCenterLabelService;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
/**
 * 规则中心标签服务service
 */
public class RuleCenterLabelServiceImpl implements RuleCenterLabelService {

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Override
    public Result<List<String>> getLabelNames(String apiCode) {

        CustomerInfoPushMainExample pushMainExample = new CustomerInfoPushMainExample();
        pushMainExample.createCriteria()
                .andPushTargetEqualTo(RuleCenterPushTargetEnum.ORIGINAL_INTERFACE.getCode())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<CustomerInfoPushMain> customerInfoPushMains = customerInfoPushMainMapper.selectByExample(pushMainExample);
        List<String> labelNames = customerInfoPushMains.stream().map(CustomerInfoPushMain::getLabelName).collect(Collectors.toList());

        return new Result<List<String>>().success().setDate(labelNames);
    }

    @Override
    public Result saveLabelTask(PushCustomerDTO dto) {
        /**
         * 先校验下 传过来的批次和 模型是否匹配
         * 推送mq
         */
        //region check
        if (dto.getBatchNumberList().size() > 50) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("批次最多选择50个");
        }
        if (dto.getmPlanNum() != null && dto.getmPlanNum() <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送数量不能小于等于0");
        }
        if (dto.getmPercentage() != null && dto.getmPercentage().compareTo(new BigDecimal(0)) <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("百分比不能小于等于0");
        }
        Integer pushNum = dto.getmPrePlanNum();
        //region insert db
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(dto.getFileIdList());
        CustomerInfoPushMain customerInfoPushMain = new CustomerInfoPushMain();

        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        List<String> showTitles = straHisFiles.stream().map(t -> t.getBatchNumber()).collect(Collectors.toList());
        customerInfoPushMain.setmApiCode(dto.getApiCode());
        customerInfoPushMain.setmRuleCondition(dto.getmRuleCondition());
        customerInfoPushMain.setmRuleConditionShow(dto.getmRuleConditionShow());
        customerInfoPushMain.setmScoreCondition(dto.getmScoreCondition());
        customerInfoPushMain.setmPercentage(dto.getmPercentage());
        customerInfoPushMain.setmPlanNum(dto.getmPlanNum());
        customerInfoPushMain.setmRealyNum(pushNum);
        Date date = new Date();
        customerInfoPushMain.setCreateTime(date);
        customerInfoPushMain.setUpdateTime(date);
        customerInfoPushMain.setmCusBatchNumberList(Joiner.on(",").join(showTitles));
        customerInfoPushMain.setmStatus(PushRuleStatusEnum.TO_BE_RUNNING.getValue());
        customerInfoPushMain.setOptUserId(String.valueOf(dto.getUserDetail().getId()));
        customerInfoPushMain.setOptUserName(dto.getUserDetail().getRealName());
        customerInfoPushMain.setLabelName(dto.getLabelName());
        customerInfoPushMain.setPushTarget(RuleCenterPushTargetEnum.ORIGINAL_INTERFACE.getCode());
        customerInfoPushMainMapper.insertSelective(customerInfoPushMain);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(customerInfoPushMain.getId().toString());
    }
}
