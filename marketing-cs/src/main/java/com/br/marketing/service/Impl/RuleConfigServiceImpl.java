package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.ScoreRuleCheckStatusEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IRuleConfigService;
import com.br.marketing.vo.CustomerScoreRuleVO;
import com.br.marketing.vo.CustomerSoleRuleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RuleConfigServiceImpl implements IRuleConfigService {

    @Resource
    ScoreRuleConfigMapper scoreRuleConfigMapper;

    @Resource
    SoleRuleConfigMapper soleRuleConfigMapper;

    @Resource
    CustomerSoleMapper customerSoleMapper;

    @Resource
    CustomerRuleMapper customerRuleMapper;

    @Resource
    CustomerMapper customerMapper;

    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    RuleRedisServiceImpl ruleRedisService;

    @Resource
    FastTaskRuleMapper fastTaskRuleMapper;

    @Resource
    FastFileRelationMapper fastFileRelationMapper;

    private final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Result<List<CustomerSoleRuleVO>> getSoleConfig(String apiCode) {

        Result<List<CustomerSoleRuleVO>> soleConfigRedis = ruleRedisService.getSoleConfigRedis(apiCode);
        if (ResultCode.SUCCESS.getValue().equals(soleConfigRedis.getCode())) {
            return soleConfigRedis;
        }

        List<CustomerSoleRuleVO> resList = new ArrayList<>();
        Customer customerByApiCode = customerMapper.getCustomerByApiCode(apiCode);
        if (customerByApiCode == null) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该用户不存在");
        }
        CustomerSoleExample customerSoleExample = new CustomerSoleExample();
        customerSoleExample.createCriteria()
                .andCustomerIdEqualTo(customerByApiCode.getId())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<CustomerSole> customerSoles = customerSoleMapper.selectByExample(customerSoleExample);
        if (customerSoles.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate(resList).setMessage("该用户没有匹配的去重规则");
        }
        List<Long> soleIds = customerSoles.stream()
                .map(t -> t.getSoleId()).collect(Collectors.toList());
        SoleRuleConfigExample soleRuleConfigExample = new SoleRuleConfigExample();
        soleRuleConfigExample.createCriteria()
                .andIdIn(soleIds)
                .andStatusEqualTo(Constants.STATUS_START)
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<SoleRuleConfig> soleRuleConfigs = soleRuleConfigMapper.selectByExample(soleRuleConfigExample);

        if (soleRuleConfigs.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该用户的去重规则是否已失效");
        }

        customerSoles.forEach(t -> {
            Optional<SoleRuleConfig> first = soleRuleConfigs.stream()
                    .filter(k -> k.getId().equals(t.getSoleId())).findFirst();
            if (first.isPresent()) {
                CustomerSoleRuleVO vo = new CustomerSoleRuleVO();
                BeanUtils.copyProperties(first.get(), vo);
                vo.setApiCode(apiCode);
                vo.setConditionInfo(t.getConditionInfo());
                vo.setAllUserType(t.getAllUserType());
                vo.setUserTypeCount(t.getUserTypeCount());
                resList.add(vo);
            }
        });
        if (resList.size() > 0) {
            resList.sort(
                    Comparator.comparing(CustomerSoleRuleVO::getAllUserType,Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(CustomerSoleRuleVO::getUserTypeCount,Comparator.nullsLast(Comparator.naturalOrder()))
            );
            ruleRedisService.setSoleConfigRedis(apiCode, resList);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(resList);
        }else{
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate("未匹配到用户的去重规则");
        }
    }

    @Override
    public Result<List<CustomerScoreRuleVO>> getScoreConfig(String apiCode) {
        List<CustomerScoreRuleVO> resList = new ArrayList<>();
        Customer customerByApiCode = customerMapper.getCustomerByApiCode(apiCode);

        CustomerRuleExample customerRuleExample = new CustomerRuleExample();
        customerRuleExample.createCriteria()
                .andCustomerIdEqualTo(customerByApiCode.getId())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<CustomerRule> customerRules = customerRuleMapper.selectByExample(customerRuleExample);
        if (customerRules.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该用户没有跑分规则");
        }
        List<Long> ruleIds = customerRules.stream()
                .map(t -> t.getRuleId()).collect(Collectors.toList());
        ScoreRuleConfigExample scoreRuleConfigExample = new ScoreRuleConfigExample();
        scoreRuleConfigExample.createCriteria()
                .andIdIn(ruleIds)
                .andStatusEqualTo(Constants.STATUS_START)
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<ScoreRuleConfig> scoreRuleConfigs = scoreRuleConfigMapper.selectByExample(scoreRuleConfigExample);

        if (scoreRuleConfigs.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该用户的去重规则是否已失效");
        }

        customerRules.forEach(t -> {
            Optional<ScoreRuleConfig> first = scoreRuleConfigs.stream()
                    .filter(k -> k.getId().equals(t.getRuleId())).findFirst();
            if (first.isPresent()) {
                CustomerScoreRuleVO vo = new CustomerScoreRuleVO();
                BeanUtils.copyProperties(first.get(), vo);
                vo.setApiCode(apiCode);
                resList.add(vo);
            }
        });
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(resList);
    }

    @Override
    public Result<List<FastTaskRule>> getFastTaskRule(String apiCode) {
        String nowDay = LocalDate.now().format(yyyyMMdd);
        FastTaskRuleExample ruleExample = new FastTaskRuleExample();
        ruleExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(1)
                .andIsDelEqualTo(1)
                .andTaskTimeLessThanOrEqualTo(nowDay);
        List<FastTaskRule> fastTaskRules = fastTaskRuleMapper.selectByExample(ruleExample);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(fastTaskRules);
    }

    /**
     * 判断是否 执行该规则 success-执行 其他-不执行
     *
     * @param rule
     * @return
     */
    @Override
    public Result checkFastTaskRule(FastTaskRule rule) {
        FastFileRelationExample relationExample = new FastFileRelationExample();
        relationExample.createCriteria()
                .andFastTaskIdEqualTo(rule.getId())
                .andIsDelEqualTo(1);
        List<FastFileRelation> fastFileRelations = fastFileRelationMapper.selectByExample(relationExample);
        if (fastFileRelations.size() > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue());
        } else {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
    }


    @Override
    public Result<List<CustomerScoreRuleVO>> getScoreConfigNow() {
        return getScoreConfigNow(null, null);
    }


    @Override
    public Result<List<CustomerScoreRuleVO>> getScoreConfigNow(List<Long> ids, String apiCode) {
        ScoreRuleConfigExample ruleConfigExample = new ScoreRuleConfigExample();
        if(ids!=null&&ids.size()>0){
            ruleConfigExample.createCriteria()
                    .andIdIn(ids)
                    .andIsDelEqualTo(Constants.DATA_VALID);
//                    .andStatusEqualTo(Constants.STATUS_START);
            ruleConfigExample.setOrderByClause(" start_time asc ");
        }else {
            String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            ruleConfigExample.createCriteria()
                    .andAutoBuildEqualTo(1)
                    .andStartTimeLessThanOrEqualTo(nowTime)
                    .andIsDelEqualTo(Constants.DATA_VALID)
                    .andStatusEqualTo(Constants.STATUS_START)
                    .andCheckStatusEqualTo(ScoreRuleCheckStatusEnum.OK.getValue());
            ruleConfigExample.setOrderByClause(" start_time asc ");
        }
        List<ScoreRuleConfig> scoreRuleConfigs = scoreRuleConfigMapper.selectByExample(ruleConfigExample);
        if (scoreRuleConfigs.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("规则不存在");
        }

        List<Long> ruleIds = scoreRuleConfigs.stream().map(t -> t.getId()).collect(Collectors.toList());
        List<String> apiCodeList = new ArrayList<>();
        if(!StringUtils.isEmpty(apiCode)){
            apiCodeList.add(apiCode);
        }
        List<CustomerScoreRuleVO> scoreRuleVoList = scoreRuleConfigMapper.getScoreRuleVoList(ruleIds, apiCodeList);
        if (CollectionUtils.isEmpty(scoreRuleVoList)) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("规则不存在");
        }

        ArrayList<CustomerScoreRuleVO> customerScoreRuleVOS = new ArrayList<>();
        for (CustomerScoreRuleVO vo : scoreRuleVoList) {
            String tableName = "b_marketing_sync_" + vo.getApiCode();
            try {
                marketingCustomerMapper.checkTableExist(tableName);
            }catch (Exception e){
                continue;
            }
            CustomerScoreRuleVO customerScoreRuleVO = new CustomerScoreRuleVO();
            BeanUtils.copyProperties(vo, customerScoreRuleVO);
            customerScoreRuleVOS.add(customerScoreRuleVO);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(customerScoreRuleVOS);
    }
}
