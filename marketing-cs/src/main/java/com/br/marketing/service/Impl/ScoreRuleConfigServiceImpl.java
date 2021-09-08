package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.vo.ScoreRuleConfigPageVO;
import com.br.marketing.vo.ScoreRuleVO;
import com.br.marketing.vo.VariableDicSelectVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private VariableDicMapper variableDicMapper;

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
        // 检查配置名称是否已经被使用过
        nameCheck(scoreRuleVO);
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
        rule.setStrategyProductShow(scoreRuleVO.getStrategyProductShow());
        rule.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        rule.setStatus(1);
        rule.setStartTime(scoreRuleVO.getStartTime());
        rule.setStrategyId(scoreRuleVO.getStrategyId());
        rule.setRuleNameShort(createNo());
        rule.setExecType(1);
        rule.setBaseInfo("");
        rule.setIsDel(1);
        rule.setCycleDay(0);
        rule.setPushType(0);
        rule.setCycleEndDay("");
        isExist(rule, scoreRuleVO.getCid(), scoreRuleVO.getApiCode());
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
                // TODO: 2021/9/7 禁用规则前要校验该规则是否正在使用
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
                .andIsDelEqualTo(1);
//                .andIsDelEqualTo(1).andStatusEqualTo(1);
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
        scoreRuleVO.setStrategyProductShow(rule.getStrategyProductShow());
        scoreRuleVO.setStrategyId(rule.getStrategyId());
        scoreRuleVO.setRuleNameShort(rule.getRuleNameShort());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(ScoreRuleVO scoreRuleVO, UserDetail userDetail) {
        if (ObjectUtils.isEmpty(userDetail)) {
            throw new BusinessException("用户信息验证失败，请重新登录重试");
        }
        // 检查配置名称是否已经被使用过
        nameCheck(scoreRuleVO);
        ScoreRuleConfig ruleConfig = scoreRuleConfigMapper.selectByPrimaryKey(scoreRuleVO.getId());
        if (ObjectUtils.isEmpty(ruleConfig)) {
            throw new BusinessException("该配置不存在");
        }
        ScoreOptLog scoreOptLog = new ScoreOptLog();
        scoreOptLog.setApicode(scoreRuleVO.getApiCode());
        scoreOptLog.setCid(scoreRuleVO.getCid());
        scoreOptLog.setScoreRuleId(String.valueOf(ruleConfig.getId()));
        scoreOptLog.setRuleName(ruleConfig.getRuleName());
        scoreOptLog.setCreateTime(new Date());
        scoreOptLog.setOptUserId(String.valueOf(userDetail.getId()));
        scoreOptLog.setOptUserName(userDetail.getUsername());
        scoreOptLog.setConditionShowInfo(ruleConfig.getConditionInfo());
        spliceConditionInfoJsonLog(scoreOptLog);
        String jsonStr = "{\"".concat("strategyId\":\"").concat(scoreRuleVO.getStrategyId())
                .concat("\",\"").concat("products\":").concat(ruleConfig.getStrategyProductShow()).concat("}");
        scoreOptLog.setStrategyProductShow(jsonStr);
        scoreOptLog.setStartTime(ruleConfig.getStartTime());
        scoreOptLog.setStatus(ruleConfig.getStatus());
        scoreOptLog.setIsDel(1);
        scoreOptLog.setUpdateTime(scoreOptLog.getCreateTime());
        int insert = scoreOptLogMapper.insert(scoreOptLog);
        if (insert < 1) {
            throw new BusinessException("变更失败，变更记录添加失败");
        }
        ScoreRuleConfig rule = new ScoreRuleConfig();
        rule.setId(scoreRuleVO.getId());
        rule.setConditionInfo(spliceConditionInfoJson(scoreRuleVO.getVdSet()));
        rule.setRuleName(scoreRuleVO.getRuleName());
        rule.setStrategyProductShow(scoreRuleVO.getStrategyProductShow());
        rule.setStartTime(scoreRuleVO.getStartTime());
        rule.setStrategyId(scoreRuleVO.getStrategyId());
        isExist(rule, scoreRuleVO.getCid(), scoreRuleVO.getApiCode());
        int i = scoreRuleConfigMapper.updateByPrimaryKeySelective(rule);
        if (i != 1) {
            throw new BusinessException("变更失败，稍后重试");
        }
    }

    /**
     * 去重
     * 规则：
     * 客户接口中的所有跑分配置不重复，根据策略、产品及场景三个属性判断数据是否重复
     *
     * @param rule    pojo
     * @param cid     客户id
     * @param apiCode 接口编码
     */
    private void isExist(ScoreRuleConfig rule, String cid, String apiCode) {
        MarketingCustomerExample example = new MarketingCustomerExample();
        example.createCriteria().andCidEqualTo(cid).andApiCodeEqualTo(apiCode);
        // 校验客户信息是否正确
        List<MarketingCustomer> customerList = marketingCustomerMapper.selectByExample(example);
        if (customerList.size() == 0) {
            throw new BusinessException("客户信息不存在或已删除");
        }
        MarketingCustomer customer = customerList.get(0);
        CustomerRuleExample crExample = new CustomerRuleExample();
        crExample.createCriteria().andCustomerIdEqualTo(customer.getId());
        // 根据客户主键获取客户下的跑分规则集合
        List<CustomerRule> customerRules = customerRuleMapper.selectByExample(crExample);
        if (customerRules == null) {
            throw new BusinessException(ServiceResultEnum.UNKNOWN_ERROR);
        }
        if (customerRules.size() < 1) {
            return;
        }
        List<Long> ruleIdList = customerRules.stream().map(CustomerRule::getRuleId).collect(Collectors.toList());
        ScoreRuleConfigExample ruleExample = new ScoreRuleConfigExample();
        ruleExample.createCriteria().andStrategyIdEqualTo(rule.getStrategyId()).andIdIn(ruleIdList);
        // 获取客户下的跑分配置
        List<ScoreRuleConfig> list = scoreRuleConfigMapper.selectByExample(ruleExample);
        if (list == null) {
            throw new BusinessException(ServiceResultEnum.UNKNOWN_ERROR);
        }
        if (list.size() < 1) {
            return;
        }
        // 产品信息获取签名
        String md501 = DigestUtils.md5DigestAsHex(rule.getStrategyProductShow().getBytes(StandardCharsets.UTF_8));
        // 场景信息获取签名
        String md510 = DigestUtils.md5DigestAsHex(rule.getConditionInfo().getBytes(StandardCharsets.UTF_8));
        for (ScoreRuleConfig src : list) {
            if (src.getId().equals(rule.getId())) {
                continue;
            }
            // 已有配置产品信息获取签名
            String md502 = DigestUtils.md5DigestAsHex(src.getStrategyProductShow().getBytes(StandardCharsets.UTF_8));
            // 已有配置场景信息获取签名
            String md511 = DigestUtils.md5DigestAsHex(src.getConditionInfo().getBytes(StandardCharsets.UTF_8));
            if (md501.equals(md502) && md510.equals(md511)) {
                throw new BusinessException("规则已经创建，建议调整历史规则");
            }
        }
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

    /**
     * 2021/9/3 19:10
     * 以天为维度生成递增的编号
     * 编码规则：日期+序号 例如：R20210903001,R20210903002,...,R20210903999
     */
    private String createNo() {
        String yyyyMMdd6 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "marketing:inner:".concat(yyyyMMdd6);
        Long index = redisChgService.incr(key);
        if (index > 999) {
            throw new BusinessException("抱歉，今天的编号已用尽，当天最大编号[".concat(yyyyMMdd6) + "999]");
        }
        LocalDateTime now = LocalDateTime.now();
        // 当前毫秒数
        long l = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime localDateTime = now.plusDays(1);
        // 第二天凌晨毫秒数
        long l1 = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long s = (l1 - l) / 1000;
        redisChgService.expire(key, (int) s);
        String prefix3 = String.format("%03d", index);
        return "R".concat(yyyyMMdd6.concat(prefix3));
    }

    /**
     * 场景json结构拼接
     */
    private void spliceConditionInfoJsonLog(ScoreOptLog scoreOptLog) {
        String json = scoreOptLog.getConditionShowInfo();
        JSONObject object = JSON.parseObject(json);
        JSONArray arrays = object.getJSONArray("operationFactor");
        List<VariableDicSelectVO> vdList = arrays.toJavaList(VariableDicSelectVO.class);
        List<String> fieldNames = vdList.stream().map(VariableDicSelectVO::getFieldName).collect(Collectors.toList());
        List<String> fieldValues = vdList.stream().map(VariableDicSelectVO::getFieldValue).collect(Collectors.toList());
        VariableDicExample example = new VariableDicExample();
        example.createCriteria()
                .andCidEqualTo(scoreOptLog.getCid())
                .andApiCodeEqualTo(scoreOptLog.getApicode())
                .andFieldNameIn(fieldNames).andFieldValueIn(fieldValues);
        List<VariableDic> variableDics = variableDicMapper.selectByExample(example);
        StringBuilder ci = new StringBuilder("{\"logicalOperation\":\"or\",\"operationFactor\":[");
        final char ch = ',';
        variableDics.forEach(vd -> ci.append("{\"fieldName\":\"")
                .append(vd.getFieldName())
                .append("\",\"fieldValue\":\"")
                .append(vd.getFieldValue())
                .append("\",\"fieldDesc\":\"")
                .append(vd.getFieldDesc())
                .append("\",\"operation\":\"=\"}").append(ch));
        // 得到最后一个字符的索引地址
        int index = ci.length() - 1;
        // 取到最后一个字符
        char c = ci.charAt(index);
        if (c == ch) {
            // 删除最后一个字符
            ci.deleteCharAt(index);
        }
        scoreOptLog.setConditionShowInfo(ci.append("]}").toString());
    }

    /**
     * 2021/9/8 15:49 规则名称校验
     */
    private void nameCheck(ScoreRuleVO scoreRuleVO) {
        ScoreRuleConfigExample ruleExample = new ScoreRuleConfigExample();
        ruleExample.createCriteria().andRuleNameEqualTo(scoreRuleVO.getRuleName()).andIsDelEqualTo(1);
        List<ScoreRuleConfig> list = scoreRuleConfigMapper.selectByExample(ruleExample);
        if (list != null && list.size() > 0) {
            throw new BusinessException("对不起，该".concat(scoreRuleVO.getRuleName()).concat("已经被使用"));
        }
    }

}
