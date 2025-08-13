package com.br.marketing.service.datagroup.rulecenter.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.MarketingRuleCenterLabelReportMapper;
import com.br.marketing.service.rulecenter.enums.RuleCenterPushTargetEnum;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.datagroup.rulecenter.RuleCenterLabelService;
import com.br.marketing.vo.RuleConditionFactorVo;
import com.br.marketing.vo.RuleConditionVo;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
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

    @Resource
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;


    @Resource
    StraHisFileMapper StraHisFileMapper;

    @Resource
    MarketingRuleCenterLabelReportMapper marketingRuleCenterLabelReportMapper;

    @Override
    public Result<Set<String>> getLabelNames(String apiCode) {

        CustomerInfoPushMainExample pushMainExample = new CustomerInfoPushMainExample();
        pushMainExample.createCriteria()
                .andMApiCodeEqualTo(apiCode)
                .andPushTargetEqualTo(RuleCenterPushTargetEnum.ORIGINAL_INTERFACE.getCode())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<CustomerInfoPushMain> customerInfoPushMains = customerInfoPushMainMapper.selectByExample(pushMainExample);
        Set<String> labelNames = customerInfoPushMains.stream().map(CustomerInfoPushMain::getLabelName).collect(Collectors.toSet());

        return new Result<List<String>>().success().setDate(labelNames);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        straHisFiles.forEach(t -> {
            CustomerInfoPushBatch customerInfoPushBatch = new CustomerInfoPushBatch();
            customerInfoPushBatch.setmId(customerInfoPushMain.getId());
            customerInfoPushBatch.setmApiCode(dto.getApiCode());
            customerInfoPushBatch.setmBatchNumber(t.getBatchNumber());
            customerInfoPushBatch.setCreateTime(date);
            customerInfoPushBatch.setUpdateTime(date);
            customerInfoPushBatch.setmFileId(t.getId());
            customerInfoPushBatchMapper.insertSelective(customerInfoPushBatch);
            //插入标签统计表
            String dataCondition = StraHisFileMapper.getCondition(t.getBatchNumber());
            List<RuleConditionVo> conditionVoList = JSON.parseObject(dataCondition, new TypeReference<List<RuleConditionVo>>() {
            }.getType());
            conditionVoList.forEach(conditionVo -> {
                List<RuleConditionFactorVo> factorVoList = conditionVo.getOperationFactor();
                String appletDate = factorVoList.stream().filter(factor -> factor.getFieldName().equals("appletDate")).findFirst().get().getFieldValue();
                String userType = factorVoList.stream().filter(factor -> factor.getFieldName().equals("userType")).findFirst().get().getFieldValue();
                MarketingRuleCenterLabelReportExample labelReportExample = new MarketingRuleCenterLabelReportExample();
                labelReportExample.createCriteria().andApiCodeEqualTo(dto.getApiCode())
                        .andLabelNameEqualTo(dto.getLabelName())
                        .andAppletDateEqualTo(appletDate)
                        .andUserTypeEqualTo(userType)
                        .andIsDelEqualTo(1);
                List<MarketingRuleCenterLabelReport> labelReportList = marketingRuleCenterLabelReportMapper.selectByExample(labelReportExample);
                //不为空，更新历史统计
                if (!CollectionUtils.isEmpty(labelReportList)) {
                    MarketingRuleCenterLabelReport update = labelReportList.get(0);
                    update.setIsDel(9);
                    marketingRuleCenterLabelReportMapper.updateByPrimaryKeySelective(update);
                }
                MarketingRuleCenterLabelReport report = new MarketingRuleCenterLabelReport();
                report.setLabelId(customerInfoPushMain.getId());
                report.setLabelName(dto.getLabelName());
                report.setApiCode(dto.getApiCode());
                report.setAppletDate(appletDate);
                report.setUserType(userType);
                marketingRuleCenterLabelReportMapper.insertSelective(report);
            });
        });
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(customerInfoPushMain.getId().toString());
    }
}
