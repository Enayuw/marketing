package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.TcyrCpaDeleteRule;
import com.br.marketing.entity.TcyrCpaDeleteRuleExample;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.TcyrCpaDeleteRuleMapper;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    @Override
    public Result rule(TcyrCpaDeleteRuleVO ruleVO) {
        TcyrCpaDeleteRule rule = new TcyrCpaDeleteRule();
        BeanUtils.copyProperties(ruleVO, rule);
        rule.setEnabled(Constants.ENABLED_FORB);
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
        calculateDeleteNum(rule);
        tcyrCpaDeleteRuleMapper.insert(rule);
        return new Result().success();
    }

    private void processRuleByType(TcyrCpaDeleteRule rule) {
        Integer ruleType = rule.getRuleType();

        switch (ruleType) {
            case 1: // 周期锁定
                String script1 = "select user_key from b_tcyr_cpa_lock_data where lock_belong = 1 and date(release_time) < curdate() and is_del = 1";
                rule.setExecuteScript(script1);
                break;
            case 2: // 大空白组
                String script2 = "select user_key from b_tcyr_cpa_blank_data where is_del = 1";
                rule.setExecuteScript(script2);
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
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), "规则类型为3时，失败类型不能为空"));
            return;
        }

        String script = generateFailMsgScript(rule.getFailMsgs());
        rule.setExecuteScript(script);
    }

    /**
     * 生成failMsg类型的执行脚本
     */
    private String generateFailMsgScript(String failMsgs) {
        if (StringUtils.isBlank(failMsgs)) {
            return ""; // 处理空输入
        }

        String[] failMsgArray = failMsgs.split(",");
        List<String> nonTwoValues = Arrays.stream(failMsgArray).map(String::trim)
                .filter(s -> !StringUtils.equals("2", s)).collect(Collectors.toList());
        boolean hasTwo = Arrays.stream(failMsgArray)
                .map(String::trim).anyMatch(s -> StringUtils.equals("2", s));

        StringBuilder script = new StringBuilder();
        if (CollectionUtils.isNotEmpty(nonTwoValues)) {
            String inClause = String.join(",", nonTwoValues);
            script.append("select user_key from b_tcyr_cpa_invalue_data where fail_msg in (")
                    .append(inClause)
                    .append(") and is_del = 1");
        }

        if (hasTwo) {
            if (script.length() > 0) {
                script.append(" union all ");
            }
            script.append("select user_key from b_tcyr_cpa_lock_data where lock_belong = 2 and date(release_time) < curdate() and is_del = 1");
        }
        return script.toString();
    }

    /**
     * 处理自定义规则（rule_type = 4）
     */
    private void processCustomRule(TcyrCpaDeleteRule rule) {
        if (StringUtils.isBlank(rule.getExecuteScript())) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), "规则类型为4时，执行脚本不能为空"));
        }
        rule.setFailMsgs(null);
    }

    /**
     * 计算剔除量级
     */
    private void calculateDeleteNum(TcyrCpaDeleteRule rule) {
        try {
            Integer deleteNum = tcyrCpaDeleteRuleMapper.calculateDeleteNumByScript(rule.getExecuteScript());
            rule.setDeleteNum(deleteNum != null ? deleteNum : 0);
        } catch (Exception e) {
            rule.setDeleteNum(0);
        }
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

        TcyrCpaDeleteRule rule = new TcyrCpaDeleteRule();
        rule.setIsDel(Constants.DATA_DEL);
        return new Result().success().setDate(tcyrCpaDeleteRuleMapper.updateByExampleSelective(rule, example));
    }
}
