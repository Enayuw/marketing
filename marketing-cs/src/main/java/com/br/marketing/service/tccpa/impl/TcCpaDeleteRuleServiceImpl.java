package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.config.biz.TcyrCpaConfigManager;
import com.br.marketing.dto.tccpa.TcCpaDeleteRuleExecuteInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.enums.TcCpaDeleteRuleSourceTypeEnum;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.TcyrCpaCollidingTaskMapper;
import com.br.marketing.mapper.TcyrCpaDeleteRuleMapper;
import com.br.marketing.service.tccpa.TcCpaCommonService;
import com.br.marketing.service.tccpa.TcCpaDataDeleteRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.tccpa.TcyrCpaDeleteRuleVO;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TcCpaDeleteRuleServiceImpl implements TcCpaDataDeleteRuleService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private TcyrCpaDeleteRuleMapper tcyrCpaDeleteRuleMapper;

    @Resource
    private TcyrCpaCollidingTaskMapper tcyrCpaCollidingTaskMapper;

    @Resource
    private TcCpaCommonService tcCpaCommonService;

    @Resource
    TcyrCpaConfigManager tcyrCpaConfigManager;

    @Override
    public Result rule(TcyrCpaDeleteRuleVO ruleVO) {
        TcyrCpaDeleteRule rule = new TcyrCpaDeleteRule();
        BeanUtils.copyProperties(ruleVO, rule);
        rule.setEnabled(Constants.ENABLED_ACT);
        rule.setIsDel(Constants.DATA_VALID);
        rule.setApiCode(marketingCommonConfig.getTcyrCpaApiCode());
        rule.setCreateTime(new Date());
        rule.setUpdateTime(new Date());
        if(rule.getRuleType().equals(1) || rule.getRuleType().equals(2)) {
            TcyrCpaDeleteRuleExample example = new TcyrCpaDeleteRuleExample();
            example.createCriteria().andIsDelEqualTo(Constants.DATA_VALID).andRuleTypeEqualTo(rule.getRuleType());

            if (tcyrCpaDeleteRuleMapper.countByExample(example) > 0) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("规则类型 " + rule.getRuleType() +
                        " 已存在有效数据，同一规则类型只能存在一条有效数据");
            }
        }
        processRuleByType(rule);
        tcyrCpaDeleteRuleMapper.insert(rule);
        return new Result().success();
    }

    private void processRuleByType(TcyrCpaDeleteRule rule) {
        Integer ruleType = rule.getRuleType();

        switch (ruleType) {
            case 1: // 周期锁定
                TcCpaDeleteRuleExecuteInfoDTO executeInfo = new TcCpaDeleteRuleExecuteInfoDTO();
                executeInfo.setSourceType(TcCpaDeleteRuleSourceTypeEnum.LOCK_DATA.getValue());
                executeInfo.setValue(Lists.newArrayList(1));
                rule.setDeleteNum(tcCpaCommonService.calculateVolume(Lists.newArrayList(executeInfo)));
                rule.setExecuteInfo(JSON.toJSONString(Lists.newArrayList(executeInfo)));
                break;
            case 2: // 大空白组
                TcCpaDeleteRuleExecuteInfoDTO executeInfo2 = new TcCpaDeleteRuleExecuteInfoDTO();
                executeInfo2.setSourceType(TcCpaDeleteRuleSourceTypeEnum.BLANK_DATA.getValue());
                rule.setDeleteNum(tcCpaCommonService.calculateVolume(Lists.newArrayList(executeInfo2)));
                rule.setExecuteInfo(JSON.toJSONString(Lists.newArrayList(executeInfo2)));
                break;
            case 3: // failMsg
                processFailMsgRule(rule);
                break;
            case 4: // 自定义
                processCustomRule(rule);
                break;
            default:
                throw new IllegalArgumentException("不支持的规则类型: " + ruleType);
        }
    }

    /**
     * 处理failMsg规则（rule_type = 3）
     */
    private void processFailMsgRule(TcyrCpaDeleteRule rule) {
        if (rule.getFailMsgs() == null || rule.getFailMsgs().trim().isEmpty()) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), "规则类型为3时，failMsgs不能为空"));
            return;
        }
        String[] failMsgArray = rule.getFailMsgs().split(",");
        //failMsg与lockBelong的映射Map
        Map<String, Integer> failMsgToLbMap = tcyrCpaConfigManager.getFailMsgToBlMapVT();
        List<Integer> lockData = Arrays.stream(failMsgArray).map(String::trim)
                .filter(failMsg -> failMsgToLbMap.containsKey(failMsg))
                .map(failMsg -> failMsgToLbMap.get(failMsg))
                .collect(Collectors.toList());
        List<Integer> invalues = Arrays.stream(failMsgArray).map(String::trim)
                .filter(failMsg -> !failMsgToLbMap.containsKey(failMsg))
                .map(Integer::parseInt).collect(Collectors.toList());

        List<TcCpaDeleteRuleExecuteInfoDTO> executeInfos = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(invalues)) {
            TcCpaDeleteRuleExecuteInfoDTO executeInfo2 = new TcCpaDeleteRuleExecuteInfoDTO();
            executeInfo2.setSourceType(TcCpaDeleteRuleSourceTypeEnum.INVALUE_DATA.getValue());
            executeInfo2.setValue(invalues);
            executeInfos.add(executeInfo2);
        }
        if (CollectionUtils.isNotEmpty(lockData)) {
            TcCpaDeleteRuleExecuteInfoDTO executeInfo = new TcCpaDeleteRuleExecuteInfoDTO();
            executeInfo.setSourceType(TcCpaDeleteRuleSourceTypeEnum.LOCK_DATA.getValue());
            executeInfo.setValue(lockData);
            executeInfos.add(executeInfo);
        }
        rule.setExecuteInfo(JSON.toJSONString(executeInfos));
        rule.setDeleteNum(tcCpaCommonService.calculateVolume(executeInfos));
    }

    /**
     * 处理自定义规则（rule_type = 4）
     */
    private void processCustomRule(TcyrCpaDeleteRule rule) {
        if (StringUtils.isBlank(rule.getExecuteInfo())) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), "规则类型为4时，执行脚本不能为空"));
        }
        rule.setFailMsgs(null);
    }

    @Override
    public PageResultReturn<TcyrCpaDeleteRuleVO> page(int page, int pageSize, String ruleName, Integer enabled) {
        TcyrCpaDeleteRuleExample example = new TcyrCpaDeleteRuleExample();
        TcyrCpaDeleteRuleExample.Criteria criteria = example.createCriteria();
        criteria.andIsDelEqualTo(Constants.DATA_VALID);
        if (StringUtils.isNotBlank(ruleName)) {
            criteria.andRuleNameLike("%" + ruleName + "%");
        }
        if (enabled != null) {
            criteria.andEnabledEqualTo(enabled);
        }
        example.setOrderByClause("update_time desc");
        List<TcyrCpaDeleteRule> packages = tcyrCpaDeleteRuleMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(packages)) {
            return PageResultReturn.setPageResult(Lists.newArrayList(), page, pageSize);
        }
        List<String> apiCodes = packages.stream().map(TcyrCpaDeleteRule::getApiCode).collect(Collectors.toList());
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeIn(apiCodes);
        Map<String, MarketingCustomer> customers = marketingCustomerMapper.selectByExample(customerExample)
                .stream().collect(Collectors.toMap(MarketingCustomer::getApiCode, customer -> customer));

        List<TcyrCpaDeleteRuleVO> packageVOS = packages.stream().map(dataPackage -> {
            TcyrCpaDeleteRuleVO vo = new TcyrCpaDeleteRuleVO();
            BeanUtils.copyProperties(dataPackage, vo);

            MarketingCustomer customer = customers.get(dataPackage.getApiCode());
            if (customer != null) {
                vo.setCid(customer.getCid());
                vo.setCustomerName(customer.getShortName());
            }
            return vo;
        }).collect(Collectors.toList());
        PageInfo<TcyrCpaDeleteRuleVO> pageInfo = new PageInfo<>(packageVOS);
        return PageResultReturn.setPageResult(packageVOS, page, pageSize, pageInfo.getTotal());
    }

    @Override
    public Result enable(Long id, Integer enabled) {
        TcyrCpaCollidingTaskExample taskExample = new TcyrCpaCollidingTaskExample();
        taskExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID)
                .andStatusLessThan(TcCpaCollidingTaskStatusEnum.STATUS_PUSH_COMPLETED.getValue())
                .andDeleteRuleIdsLike("%" + id + "%");
        if(tcyrCpaCollidingTaskMapper.countByExample(taskExample) > 0 && Objects.equals(enabled, Constants.ENABLED_FORB)) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("存在已使用该规则的撞库任务，不能禁用剔除规则");
        }

        TcyrCpaDeleteRuleExample example = new TcyrCpaDeleteRuleExample();
        example.createCriteria().andIdEqualTo(id);

        TcyrCpaDeleteRule rule = new TcyrCpaDeleteRule();
        rule.setEnabled(enabled);
        return new Result().success().setDate(tcyrCpaDeleteRuleMapper.updateByExampleSelective(rule, example));
    }

    @Override
    public Result delete(Long id) {
        TcyrCpaDeleteRuleExample example = new TcyrCpaDeleteRuleExample();
        example.createCriteria().andIdEqualTo(id);

        TcyrCpaCollidingTaskExample taskExample = new TcyrCpaCollidingTaskExample();
        taskExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID)
                .andStatusLessThan(TcCpaCollidingTaskStatusEnum.STATUS_PUSH_COMPLETED.getValue())
                .andDeleteRuleIdsLike("%" + id + "%");
        if(tcyrCpaCollidingTaskMapper.countByExample(taskExample) > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("存在已使用该规则的撞库任务，不能删除剔除规则");
        }

        TcyrCpaDeleteRule rule = new TcyrCpaDeleteRule();
        rule.setIsDel(Constants.DATA_DEL);
        return new Result().success().setDate(tcyrCpaDeleteRuleMapper.updateByExampleSelective(rule, example));
    }
}
