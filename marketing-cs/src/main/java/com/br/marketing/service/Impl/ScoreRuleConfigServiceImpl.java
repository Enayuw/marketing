package com.br.marketing.service.Impl;

import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.CustomerRule;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.ScoreRuleConfig;
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

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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

    @Resource
    private ScoreOptLogMapper scoreOptLogMapper;

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
            throw new BusinessException(ServiceResultEnum.SUCCESS_5.getCode(), "客户信息不存在或已删除");
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
        rule.setUpdateTime(rule.getCreateTime());
        int insert1 = scoreRuleConfigMapper.insert(rule);
        if (insert1 == 1) {
            CustomerRule cr = new CustomerRule();
            cr.setRuleId(rule.getId());
            cr.setCustomerId(customer.getId());
            cr.setCreateTime(new Date());
            cr.setIsDel(1);
            cr.setUpdateTime(cr.getCreateTime());
            int insert2 = customerRuleMapper.insert(cr);
            if (insert2 > 0) {
                return;
            }
        }
        throw new BusinessException("配置保存失败，请稍后重试！");
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
