package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.enums.ScoreRuleCheckStatusEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CustomerScoreRuleDTO;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.CustomerRuleMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingTaskExtendMapper;
import com.br.marketing.mapper.ScoreRuleConfigMapper;
import com.br.marketing.service.ScoreOptLogService;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.CustomerScoreRuleVO;
import com.br.marketing.vo.ScoreRuleConfigPageVO;
import com.br.marketing.vo.ScoreRuleVO;
import com.br.marketing.vo.VariableDicSelectVO;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private ScoreOptLogService scoreOptLogService;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Autowired
    SoleStrategyService soleStrategyService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public PageResultReturn findListPage(int page, int pageSize, String search, Integer status, String cts,
                                         String cte, String uts, String ute, Integer execType) {
        PageHelper.startPage(page, pageSize);
        try {
            List<ScoreRuleConfigPageVO> list = scoreRuleConfigMapper.findList(search, status, cts, cte, uts, ute, execType);
            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void save(ScoreRuleVO scoreRuleVO, MarketingUserDetail userDetail) {
        try {
            List<String> apiCodes = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(scoreRuleVO.getApiCode());
            boolean nonStrategy = scoreRuleVO.getTaskType() != null && scoreRuleVO.getTaskType() == 1;
            String errorMsg = "很遗憾小主，以下ApiCode【%s】禁止跑分！";
            List<String> errorApiCodes = checkApiCodes(apiCodes);
            if (nonStrategy || CollectionUtils.isEmpty(errorApiCodes)) {
                ScoreRuleConfigServiceImpl service = (ScoreRuleConfigServiceImpl) AopContext.currentProxy();
                service.saveTransaction(scoreRuleVO, userDetail);
            } else {
                String errorApiCode = Joiner.on(",").join(errorApiCodes);
                throw new BusinessException(String.format(errorMsg, errorApiCode));
            }
        } catch (Exception e) {
            String yyyyMMdd6 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String key = "marketing:inner:".concat(yyyyMMdd6);
            redisChgService.incrBy(key, -1L);
            redisChgService.expire(key, getKeyExpiration());
            String msg = "很遗憾小主，配置保存失败";
            if (e instanceof BusinessException) {
                msg = ((BusinessException) e).getMsg();
            }
            throw new BusinessException(msg);
        }
    }

    private List<String> checkApiCodes(List<String> apiCodes) {
        List<String> errorApiCodes = Lists.newArrayList();
        JSONObject allowScoreTaskConfig = marketingCommonConfig.getAllowScoreTaskConfig();
        List<String> allowScoreTaskApiType = allowScoreTaskConfig.getJSONArray("allowScoreTaskApiType").toJavaList(String.class);
        List<String> allowScoreTaskApiCode = allowScoreTaskConfig.getJSONArray("allowScoreTaskApiCode").toJavaList(String.class);
        for (String apiCode : apiCodes) {
            MarketingCustomerExample example = new MarketingCustomerExample();
            example.createCriteria().andApiCodeEqualTo(apiCode);
            List<MarketingCustomer> select = marketingCustomerMapper.selectByExample(example);
            MarketingCustomer customer = select.get(0);
            if (!allowScoreTaskApiType.contains(customer.getApiType()) && !allowScoreTaskApiCode.contains(customer.getApiCode())) {
                errorApiCodes.add(apiCode);
            }
        }
        return errorApiCodes;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveTransaction(ScoreRuleVO scoreRuleVO, MarketingUserDetail userDetail) throws Exception {
        // 检查配置名称是否已经被使用过
        nameCheck(scoreRuleVO);
        // 2024-07-18 修改为多apiCode
        List<Map<String, Object>> variableList = scoreRuleVO.getVariableList();
        List<String> apiCodeList = new ArrayList<>();
        HashMap<String, Set<VariableDicSelectVO>> vdOfApiCodeMap = AssembleApiCodeToVdSetMap(variableList, apiCodeList);

        MarketingCustomerExample example = new MarketingCustomerExample();
        example.createCriteria().andCidEqualTo(scoreRuleVO.getCid())
                .andApiCodeIn(apiCodeList)
                .andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> customerList = marketingCustomerMapper.selectByExample(example);
        if (customerList.size() == 0) {
            throw new BusinessException("抱歉小主，客户不存在或已删除");
        }

        ScoreRuleConfig rule = new ScoreRuleConfig();
        // rule.setConditionInfo(spliceConditionInfoJson(scoreRuleVO.getVdSet()));
        rule.setRuleName(scoreRuleVO.getRuleName());
        rule.setStrategyProductShow(scoreRuleVO.getStrategyProductShow());
        rule.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        rule.setUpdateTime(rule.getCreateTime());
        rule.setStatus(1);
        rule.setStartTime(scoreRuleVO.getStartTime());
        rule.setStrategyId(scoreRuleVO.getStrategyId());
        rule.setExecType(scoreRuleVO.getExecType());
        rule.setBaseInfo(scoreRuleVO.getBaseInfo());
        rule.setIsDel(1);
        rule.setCycleDay(scoreRuleVO.getCycleDay());
        rule.setPushType(0);
        rule.setCycleEndDay(scoreRuleVO.getCycleEndDay());
        rule.setStrategyProductJson(scoreRuleVO.getStrategyProductJson());
        rule.setTaskType(scoreRuleVO.getTaskType() != null ? scoreRuleVO.getTaskType() : Integer.valueOf(0));
        rule.setProductInfo(scoreRuleVO.getProductInfo());
        rule.setThreekEncryptType(scoreRuleVO.getThreekEncryptType());
        rule.setIsOnline(scoreRuleVO.getIsOnline());
        if (scoreRuleVO.getExecType() == 4) {
            rule.setAutoBuild(1);
            rule.setIsStackValidity(scoreRuleVO.getIsStackValidity());
        }
        rule.setPriority(scoreRuleVO.getPriority() == null ? 9 : scoreRuleVO.getPriority());
        rule.setCheckStatus(ScoreRuleCheckStatusEnum.OK.getValue());

        // 2024-07-19 修改为多apiCode校验
        for (String apiCodeItem : apiCodeList) {
            String s = spliceConditionInfoJson(vdOfApiCodeMap.get(apiCodeItem));
            isExist(rule, scoreRuleVO.getCid(), apiCodeItem, s);
        }

        rule.setRuleNameShort(createNo());
        int insertRule = scoreRuleConfigMapper.insert(rule);
        if (insertRule != 1) {
            throw new BusinessException("很遗憾小主，配置保存失败");
        }

        for (MarketingCustomer customer : customerList) {
            CustomerRule cr = new CustomerRule();
            cr.setRuleId(rule.getId());
            cr.setCustomerId(customer.getId());
            Set<VariableDicSelectVO> vdSet = vdOfApiCodeMap.get(customer.getApiCode());
            if (CollectionUtils.isEmpty(vdSet)) {
                throw new BusinessException("很遗憾小主，配置保存失败");
            }
            cr.setConditionInfo(spliceConditionInfoJson(vdSet));
            cr.setCreateTime(new Date());
            cr.setIsDel(1);
            int insertCustomerRule = customerRuleMapper.insert(cr);
            if (insertCustomerRule < 1) {
                throw new BusinessException("很遗憾小主，配置保存失败");
            }
            ScoreRuleVO ScoreRuleNew = new ScoreRuleVO();
            BeanUtils.copyProperties(scoreRuleVO, ScoreRuleNew);
            ScoreRuleNew.setId(rule.getId());
            // 记录变更日志
            scoreOptLogService.save(ScoreRuleNew, rule.getStatus(), userDetail, spliceConditionInfoJson(vdSet));
        }
    }

    @Override
    public boolean setStatus(Long rid, Long crId, Integer status, MarketingUserDetail userDetail) {
        ScoreRuleConfig rule = scoreRuleConfigMapper.selectByPrimaryKey(rid);
        if (ObjectUtils.isEmpty(rule) || rule.getIsDel() != 1) {
            throw new BusinessException("抱歉小主，规则无效或不存在");
        }
        if (rule.getStatus().equals(status)) {
            return true;
        }
//        CustomerRule customerRule = customerRuleMapper.selectByPrimaryKey(crId);
//        if (!rule.getId().equals(customerRule.getRuleId())) {
//            throw new BusinessException("抱歉小主，数据异常");
//        }
        ScoreRuleConfig ruleConfig = new ScoreRuleConfig();
        ruleConfig.setId(rid);
        switch (status) {
            case 1:
                ruleConfig.setCheckStatus(ScoreRuleCheckStatusEnum.OK.getValue());
                ruleConfig.setStatus(status);
                ruleConfig.setUpdateTime(new Date());
                break;
            case 2:
            case 3:
                // TODO: 2021/9/7 禁用规则前要校验该规则是否正在使用
                ruleConfig.setStatus(status);
                ruleConfig.setUpdateTime(new Date());
                break;
            default:
                throw new BusinessException("警告小主，非法的状态");
        }
        int i = scoreRuleConfigMapper.updateByPrimaryKeySelective(ruleConfig);
        if (i < 1) {
            throw new BusinessException("很遗憾小主，操作失败");
        }

        List<HashMap<String, Object>> customerAndUserType = customerRuleMapper.getCustomerAndUserType(rid);
        for (HashMap<String, Object> res : customerAndUserType) {
            ScoreRuleVO scoreRuleVO = new ScoreRuleVO();
            BeanUtils.copyProperties(rule, scoreRuleVO, ScoreRuleVO.class);
            scoreRuleVO.setCid((String) res.get("cid"));
            scoreRuleVO.setApiCode((String) res.get("apiCode"));
            scoreRuleVO.setId(rid);
            // 记录变更日志
            scoreOptLogService.save(scoreRuleVO, status, userDetail, (String) res.get("conditionInfo"));
        }
        return true;
    }

    @Override
    public ScoreRuleVO detail(Long rid, Long crId) {
        List<Long> ruleIdList = new ArrayList<>();
        ruleIdList.add(rid);
        List<CustomerScoreRuleVO> scoreRuleVoList = scoreRuleConfigMapper.getScoreRuleVoList(ruleIdList, null);
        if (CollectionUtils.isEmpty(scoreRuleVoList)) {
            throw new BusinessException("抱歉小主，此规则不存在或已删除");
        }

        ScoreRuleConfig rule = scoreRuleVoList.get(0);

        String cid = scoreRuleVoList.get(0).getCid();
        String apiCodes = scoreRuleVoList.stream().map(CustomerScoreRuleVO::getApiCode).collect(Collectors.joining(","));

        ScoreRuleVO scoreRuleVO = new ScoreRuleVO();
        scoreRuleVO.setId(rule.getId());
        scoreRuleVO.setRuleName(rule.getRuleName());
        scoreRuleVO.setStartTime(rule.getStartTime());
        scoreRuleVO.setStrategyProductShow(rule.getStrategyProductShow());
        scoreRuleVO.setStrategyId(rule.getStrategyId());
        scoreRuleVO.setRuleNameShort(rule.getRuleNameShort());
        // TODO 2024-07-24
        // scoreRuleVO.setVdSet(getVdSet(rule.getConditionInfo()));
        scoreRuleVO.setVariableList(getVariableList(scoreRuleVoList));
        scoreRuleVO.setApiCode(apiCodes);
        scoreRuleVO.setCid(cid);
        scoreRuleVO.setExecType(rule.getExecType());
        scoreRuleVO.setBaseInfo(rule.getBaseInfo());
        scoreRuleVO.setCycleDay(rule.getCycleDay());
        scoreRuleVO.setCycleEndDay(rule.getCycleEndDay());
        scoreRuleVO.setTaskType(rule.getTaskType());
        scoreRuleVO.setProductInfo(rule.getProductInfo());
        scoreRuleVO.setStrategyProductJson(rule.getStrategyProductJson());
        scoreRuleVO.setThreekEncryptType(rule.getThreekEncryptType());
        scoreRuleVO.setIsOnline(rule.getIsOnline());
        scoreRuleVO.setPriority(rule.getPriority());
        scoreRuleVO.setIsStackValidity(rule.getIsStackValidity());
        return scoreRuleVO;
    }

    /**
     * 2021/9/11 11:43
     * 解析json
     */
    private Set<VariableDicSelectVO> getVdSet(String json) {
        JSONObject object = JSON.parseObject(json);
        JSONArray arrays = object.getJSONArray("operationFactor");
        List<VariableDicSelectVO> vdList = arrays.toJavaList(VariableDicSelectVO.class);
        return new HashSet<>(vdList);
    }

    private List<Map<String, Object>> getVariableList(List<CustomerScoreRuleVO> scoreRuleVoList) {
        List<Map<String, Object>> variableList = new ArrayList<>();
        for (CustomerScoreRuleVO vo : scoreRuleVoList) {
            Map<String, Object> variableMap = new HashMap<>();
            variableMap.put("apiCode", vo.getApiCode());
            variableMap.put("vdSet", getVdSet(vo.getConditionInfo()));
            variableList.add(variableMap);
        }
        return variableList;
    }

    @Override
    public ScoreRuleConfig getScoreRule(Long ruleId) {
        return scoreRuleConfigMapper.selectByPrimaryKey(ruleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(ScoreRuleVO scoreRuleVO, MarketingUserDetail userDetail) {
        if (ObjectUtils.isEmpty(userDetail)) {
            throw new BusinessException("素不相识的小主，请登录后重试");
        }
        // 检查配置名称是否已经被使用过
        nameCheck(scoreRuleVO);
        ScoreRuleConfig rule = new ScoreRuleConfig();
        rule.setId(scoreRuleVO.getId());
        // TODO 2024-07-24
        // rule.setConditionInfo(spliceConditionInfoJson(scoreRuleVO.getVdSet()));
        rule.setRuleName(scoreRuleVO.getRuleName());
        rule.setStrategyProductShow(scoreRuleVO.getStrategyProductShow());
        rule.setStartTime(scoreRuleVO.getStartTime());
        rule.setStrategyId(scoreRuleVO.getStrategyId());
        rule.setExecType(scoreRuleVO.getExecType());
        rule.setBaseInfo(scoreRuleVO.getBaseInfo());
        rule.setCycleDay(scoreRuleVO.getCycleDay());
        rule.setCycleEndDay(scoreRuleVO.getCycleEndDay());
        rule.setStrategyProductJson(scoreRuleVO.getStrategyProductJson());
        rule.setTaskType(scoreRuleVO.getTaskType() != null ? scoreRuleVO.getTaskType() : 0);
        rule.setProductInfo(scoreRuleVO.getProductInfo());
        rule.setThreekEncryptType(scoreRuleVO.getThreekEncryptType());
        rule.setIsOnline(scoreRuleVO.getIsOnline());
        if (scoreRuleVO.getExecType() == 4) {
            rule.setAutoBuild(1);
        }

        rule.setIsStackValidity(scoreRuleVO.getIsStackValidity());
        rule.setPriority(scoreRuleVO.getPriority() == null ? 9 : scoreRuleVO.getPriority());
        // 默认开启
        rule.setStatus(1);
        rule.setCheckStatus(ScoreRuleCheckStatusEnum.OK.getValue());
        rule.setUpdateTime(new Date());

        // 2024-07-24 修改为多apiCode
        List<String> apiCodeList = new ArrayList<>();
        List<Map<String, Object>> variableList = scoreRuleVO.getVariableList();
        HashMap<String, Set<VariableDicSelectVO>> apiCodeToVdSetMap = AssembleApiCodeToVdSetMap(variableList, apiCodeList);

        // 2024-07-19 修改为多apiCode校验
        for (String apiCodeItem : apiCodeList) {
            Set<VariableDicSelectVO> variableDicSelectVOS = apiCodeToVdSetMap.get(apiCodeItem);
            isExist(rule, scoreRuleVO.getCid(), apiCodeItem, spliceConditionInfoJson(variableDicSelectVOS));
        }

        int i = scoreRuleConfigMapper.updateByPrimaryKeySelective(rule);
        if (i != 1) {
            throw new BusinessException("很遗憾小主，变更失败");
        }

        List<Long> ruleIdList = new ArrayList<>();
        ruleIdList.add(scoreRuleVO.getId());
        List<CustomerScoreRuleDTO> scoreRuleDtoList = scoreRuleConfigMapper.getScoreRuleDtoList(ruleIdList, null);
        if (CollectionUtils.isEmpty(scoreRuleDtoList)) {
            throw new BusinessException("很遗憾小主，变更失败");
        }

        // apiCode变更
        for (CustomerScoreRuleDTO dto : scoreRuleDtoList) {
            String apiCode = dto.getApiCode();
            boolean isFind = false;
            Set<VariableDicSelectVO> vdSet = apiCodeToVdSetMap.get(apiCode);
            if (!CollectionUtils.isEmpty(vdSet)) {
                isFind = true;
            }
            // customerRuleExample
            CustomerRuleExample customerRuleExample = new CustomerRuleExample();
            customerRuleExample.createCriteria().andCustomerIdEqualTo(dto.getMid()).andRuleIdEqualTo(dto.getId());
            // 参数里没有该apiCode, 已有配置有该apiCode, 删除该apiCode对应配置
            if (!isFind) {
                customerRuleMapper.deleteByExample(customerRuleExample);
                continue;
            }
            // 参数有该apiCode, 已有配置有该apiCode, 更新该apiCode对应配置
            CustomerRule customerRule = new CustomerRule();
            customerRule.setConditionInfo(spliceConditionInfoJson(vdSet));
            int update = customerRuleMapper.updateByExampleSelective(customerRule, customerRuleExample);
            if (update != 1) {
                throw new BusinessException("很遗憾小主，变更失败");
            }
            // 记录变更日志
            ScoreRuleVO ruleVo = new ScoreRuleVO();
            BeanUtils.copyProperties(scoreRuleVO, ruleVo);
            ruleVo.setApiCode(apiCode);
            ruleVo.setId(scoreRuleVO.getId());
            scoreOptLogService.save(ruleVo, rule.getStatus(), userDetail, spliceConditionInfoJson(vdSet));
        }
    }

    private HashMap<String, Set<VariableDicSelectVO>> AssembleApiCodeToVdSetMap(List<Map<String, Object>> variableList, List<String> apiCodeList) {
        HashMap<String, Set<VariableDicSelectVO>> apiCodeToVdSetMap = new HashMap<>();
        if (CollectionUtils.isEmpty(variableList)) {
            return apiCodeToVdSetMap;
        }

        variableList.forEach((Map<String, Object> item) -> {
            if (StringUtils.isEmpty(item.get("apiCode"))) {
                log.warn("入参apiCode不正确");
                throw new BusinessException("抱歉小主，变更失败");
            }
            String apiCode = String.valueOf(item.get("apiCode"));
            Object vdSetObject = item.get("vdSet");
            if (vdSetObject == null) {
                log.warn("入参vdSet不正确");
                throw new BusinessException("抱歉小主，变更失败");
            }
            Set<VariableDicSelectVO> vdSet = new HashSet<>();
            for (LinkedHashMap variableDicSelectVO : (ArrayList<LinkedHashMap>) vdSetObject) {
                VariableDicSelectVO variableDicSelectVO1 = new VariableDicSelectVO();
                variableDicSelectVO1.setFieldName((String) variableDicSelectVO.get("fieldName"));
                variableDicSelectVO1.setFieldValue((String) variableDicSelectVO.get("fieldValue"));
                variableDicSelectVO1.setFieldDesc((String) variableDicSelectVO.get("fieldDesc"));
                vdSet.add(variableDicSelectVO1);
            }
            apiCodeToVdSetMap.put(apiCode, vdSet);
            apiCodeList.add(apiCode);
        });
        return apiCodeToVdSetMap;
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
    private void isExist(ScoreRuleConfig rule, String cid, String apiCode, String conditionInfo) {
        MarketingCustomerExample example = new MarketingCustomerExample();
        example.createCriteria().andCidEqualTo(cid).andApiCodeEqualTo(apiCode);
        // 校验客户信息是否正确
        List<MarketingCustomer> customerList = marketingCustomerMapper.selectByExample(example);
        if (customerList.size() == 0) {
            throw new BusinessException("很遗憾小主，客户["
                    .concat(cid)
                    .concat(":")
                    .concat(apiCode)
                    .concat("]不存在或已删除"));
        }
        MarketingCustomer customer = customerList.get(0);
        CustomerRuleExample crExample = new CustomerRuleExample();
        crExample.createCriteria()
                .andCustomerIdEqualTo(customer.getId())
                .andIsDelEqualTo(Constants.DATA_VALID);
        // 根据客户主键获取客户下的跑分规则集合
        List<CustomerRule> customerRules = customerRuleMapper.selectByExample(crExample);
        if (customerRules == null) {
            throw new BusinessException(ServiceResultEnum.UNKNOWN_ERROR);
        }
        if (customerRules.size() < 1) {
            return;
        }
        List<Long> ruleIdList = new ArrayList<>();
        HashMap<Long, CustomerRule> customerRuleMap = new HashMap();
        for (CustomerRule customerRule : customerRules) {
            ruleIdList.add(customerRule.getRuleId());
            customerRuleMap.put(customerRule.getRuleId(), customerRule);
        }
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
        // TODO
        String md510 = DigestUtils.md5DigestAsHex(conditionInfo.getBytes(StandardCharsets.UTF_8));
        for (ScoreRuleConfig src : list) {
            if (src.getId().equals(rule.getId())) {
                continue;
            }
            CustomerRule customerRule = customerRuleMap.get(src.getId());
            if (customerRule == null) {
                continue;
            }
            // 已有配置产品信息获取签名
            String md502 = DigestUtils.md5DigestAsHex(src.getStrategyProductShow().getBytes(StandardCharsets.UTF_8));
            // 已有配置场景信息获取签名

            String md511 = StringUtils.isNotBlank(customerRule.getConditionInfo()) ?
                    DigestUtils.md5DigestAsHex(customerRule.getConditionInfo().getBytes(StandardCharsets.UTF_8))
                    : "";
            if (md501.equals(md502) && md510.equals(md511)) {
                throw new BusinessException("报告小主，找到相似的规则["
                        .concat(src.getRuleName())
                        .concat("(")
                        .concat(src.getRuleNameShort())
                        .concat(")],建议使用该规则"));
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
     * 编码规则：R+日期+序号（三位） 例如：R20210903001,R20210903002,...,R20210903999
     */
    private String createNo() {
        String yyyyMMdd6 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "marketing:inner:".concat(yyyyMMdd6);
        Long index = redisChgService.incr(key);
        if (index > 999) {
            throw new BusinessException("很遗憾小主，今天的规则编号(".concat(yyyyMMdd6) + "999)已经用尽");
        }
        redisChgService.expire(key, getKeyExpiration());
        String prefix3 = String.format("%03d", index);
        return "R".concat(yyyyMMdd6.concat(prefix3));
    }


    /**
     * 获取当前时间到第二天凌晨的秒
     *
     * @dateTime 2021/10/19 9:21
     */
    private int getKeyExpiration() {
        LocalDateTime now = LocalDateTime.now();
        // 当前毫秒数
        long l = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime localDateTime = now.plusDays(1);
        // 第二天凌晨毫秒数
        long l1 = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (int) (l1 - l) / 1000;
    }


    /**
     * 2021/9/8 15:49 规则名称校验
     */
    private void nameCheck(ScoreRuleVO scoreRuleVO) {
        ScoreRuleConfigExample ruleExample = new ScoreRuleConfigExample();
        ruleExample.createCriteria().andRuleNameEqualTo(scoreRuleVO.getRuleName()).andIsDelEqualTo(1);
        List<ScoreRuleConfig> list = scoreRuleConfigMapper.selectByExample(ruleExample);
        if (list != null && list.size() > 0) {
            if (!ObjectUtils.isEmpty(scoreRuleVO.getId())) {
                List<ScoreRuleConfig> collect = list.stream().filter(
                                scoreRuleConfig -> !scoreRuleConfig.getId().equals(scoreRuleVO.getId()))
                        .collect(Collectors.toList());
                if (collect.size() < 1) {
                    return;
                }
            }
            throw new BusinessException("哇塞小主，“".concat(scoreRuleVO.getRuleName()).concat("”已经被使用"));
        }
    }


    @Override
    public Result<List<String>> getDataCondition(MarketingTaskExtend taskExtend, MarketingTask task, String date) {
        Result<List<String>> listResult = soleStrategyService.analysisConditions(taskExtend.getDataCondition());
        return listResult;
//        Result<Boolean> dataType = isSelectRuleByTask(taskExtend.getTaskId());
//        if(!ResultCode.SUCCESS.getValue().equals(dataType.getCode())){
//            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(dataType.getMessage());
//        }
//        if(dataType.getData()){
//            Result<List<String>> listResult = soleStrategyService.analysisConditions(taskExtend.getDataCondition());
//            return listResult;
//        }else{
//            Result<String> stringResult = soleStrategyService.analysisCondition(taskExtend.getDataCondition());
//            if(ResultCode.SUCCESS.getValue().equals(stringResult.getCode())){
//                ArrayList<String> strings = new ArrayList<>();
//                strings.add(soleStrategyService.analysisSimpleConditionPlus(stringResult.getData(),date,date.concat(" ").concat(task.getStartTime
//                ()).concat(":00")));
//                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(strings);
//            }
//            else{
//                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(stringResult.getMessage());
//            }
//        }
    }

    @Override
    public Integer getPart(Integer count, Integer index) {
        return null;
    }
}
