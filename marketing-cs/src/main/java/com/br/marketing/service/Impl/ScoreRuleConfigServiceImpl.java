package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerRuleMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.ScoreOptLogMapper;
import com.br.marketing.mapper.ScoreRuleConfigMapper;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.vo.ScoreRuleConfigPageVO;
import com.br.marketing.vo.ScoreRuleVO;
import com.br.marketing.vo.VariableDicSelectVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 跑分配置业务实现
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/8/31 14:36
 */
@Service
@Slf4j
public class ScoreRuleConfigServiceImpl implements ScoreRuleConfigService {

    @Resource
    private ScoreRuleConfigMapper scoreRuleConfigMapper;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private CustomerRuleMapper customerRuleMapper;

    @Override
    public PageResultReturn findListPage(int page, int pageSize, String search, Integer status, String cts, String cte, String uts, String ute) {
        PageHelper.startPage(page, pageSize);
        try {
            List<ScoreRuleConfigPageVO> list = scoreRuleConfigMapper.findList(search, status, cts, cte, uts, ute);
            return PageResultReturn.setPageResult(list, page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(ScoreRuleVO scoreRuleVO) {
        MarketingCustomerExample example = new MarketingCustomerExample();
        example.createCriteria().andCidEqualTo(scoreRuleVO.getCid())
                .andApiCodeEqualTo(scoreRuleVO.getApiCode())
                .andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> customerList = marketingCustomerMapper.selectByExample(example);
        if (customerList.size() == 0) {
            throw new BusinessException("客户信息不存在或已删除");
        }
        MarketingCustomer customer = customerList.get(0);
        ScoreRuleConfig rule = new ScoreRuleConfig();
        rule.setConditionInfo(spliceConditionInfoJson(scoreRuleVO.getVdSet()));
        rule.setRuleName(scoreRuleVO.getRuleName());
        rule.setStrategyProductJson(scoreRuleVO.getStrategyProductJson());
        rule.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        rule.setStatus(1);
        rule.setStartTime(scoreRuleVO.getStartTime());
        rule.setStrategyId(scoreRuleVO.getStrategyId());
        rule.setRuleNameShort("");
        rule.setExecType(1);
        rule.setBaseInfo("");
        rule.setIsDel(1);
        rule.setCycleDay(0);
        rule.setPushType(0);
        rule.setCycleEndDay("");
        int insert1 = scoreRuleConfigMapper.insert(rule);
        if (insert1 == 1) {
            CustomerRule cr = new CustomerRule();
            cr.setRuleId(rule.getId());
            cr.setCustomerId(customer.getId());
            cr.setCreateTime(new Date());
            cr.setIsDel(1);
            int insert2 = customerRuleMapper.insert(cr);
            if (insert2 > 0) {
                return;
            }
        }
        throw new BusinessException("配置保存失败，请稍后重试！");
    }

    @Override
    public boolean setStatus(Long rid, Integer status) {
        ScoreRuleConfig rule = scoreRuleConfigMapper.selectByPrimaryKey(rid);
        if (ObjectUtils.isEmpty(rule) || rule.getIsDel() != 1) {
            throw new BusinessException("抱歉，规则无效或不存在");
        }
        if (rule.getStatus().equals(status)) {
            return true;
        }
        ScoreRuleConfig ruleConfig = new ScoreRuleConfig();
        ruleConfig.setId(rid);
        switch (status) {
            case 1:
            case 2:
            case 3:
                ruleConfig.setStatus(status);
                break;
            default:
                throw new BusinessException("警告，非法的状态");
        }
        int i = scoreRuleConfigMapper.updateByPrimaryKeySelective(ruleConfig);
        return i == 1;
    }

    @Override
    public ScoreRuleVO detail(Long rid, Long crId) {
        CustomerRule customerRule = customerRuleMapper.selectByPrimaryKey(crId);
        if (ObjectUtils.isEmpty(customerRule) || !customerRule.getRuleId().equals(rid) || customerRule.getIsDel() != 1) {
            throw new BusinessException("抱歉，数据异常或已删除");
        }
        ScoreRuleConfigExample example = new ScoreRuleConfigExample();
        example.createCriteria().andIdEqualTo(rid)
                .andIsDelEqualTo(1).andStatusEqualTo(1);
        List<ScoreRuleConfig> list = scoreRuleConfigMapper.selectByExample(example);
        if (ObjectUtils.isEmpty(list) || list.size() < 1) {
            throw new BusinessException("抱歉，此规则不存在或已禁用");
        }
        MarketingCustomerExample mcExample = new MarketingCustomerExample();
        mcExample.createCriteria().andIdEqualTo(customerRule.getCustomerId()).andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> customerList = marketingCustomerMapper.selectByExample(mcExample);
        if (customerList.size() == 0) {
            throw new BusinessException("客户信息不存在或已删除");
        }
        ScoreRuleConfig rule = list.get(0);
        MarketingCustomer customer = customerList.get(0);
        ScoreRuleVO scoreRuleVO = new ScoreRuleVO();
        scoreRuleVO.setId(rule.getId());
        scoreRuleVO.setRuleName(rule.getRuleName());
        scoreRuleVO.setStartTime(rule.getStartTime());
        scoreRuleVO.setStrategyProductJson(rule.getStrategyProductJson());
        scoreRuleVO.setStrategyId(rule.getStrategyId());
        String json = rule.getConditionInfo();
        JSONObject object = JSON.parseObject(json);
        JSONArray arrays = object.getJSONArray("operationFactor");
        List<VariableDicSelectVO> vdList = arrays.toJavaList(VariableDicSelectVO.class);
        scoreRuleVO.setVdSet(new HashSet<>(vdList));
        scoreRuleVO.setApiCode(customer.getApiCode());
        scoreRuleVO.setCid(customer.getCid());
        return scoreRuleVO;
    }

    @Override
    public ScoreRuleConfig getScoreRule(Long ruleId) {
        return scoreRuleConfigMapper.selectByPrimaryKey(ruleId);
    }


    /**
     * 场景json结构拼接
     */
    private String spliceConditionInfoJson(Set<VariableDicSelectVO> set) {
        /*
         * condition_info 存储信息数据结构:
         * {
         *     "logicalOperation": "or",
         *     "operationFactor": [
         *         {
         *             "fieldName": "user_type",
         *             "fieldValue": "S01",
         *             "operation": "="
         *         }
         *     ]
         * }
         *
         * 属性说明：
         * fieldName——字段名称
         * fieldValue——字段值
         * operation——运算符（= :等于；in : 数组内包含）
         *
         * 存储结构举个栗子：
         * {"logicalOperation":"or","operationFactor":[{"fieldName":"user_type","fieldValue":"S01","operation":"="}]}
         */
        StringBuilder ci = new StringBuilder("{\"logicalOperation\":\"or\",\"operationFactor\":[");
        final char ch = ',';
        set.forEach(vd -> ci.append("{\"fieldName\":\"")
                .append(vd.getFieldName())
                .append("\",\"fieldValue\":\"")
                .append(vd.getFieldValue())
                .append("\",\"operation\":\"=\"}").append(ch));
        // 得到最后一个字符的索引地址
        int index = ci.length() - 1;
        // 取到最后一个字符
        char c = ci.charAt(index);
        if (c == ch) {
            // 删除最后一个字符
            ci.deleteCharAt(index);
        }
        return ci.append("]}").toString();
    }

}
