package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.*;
import com.br.marketing.service.RuleOfSoleService;
import com.br.marketing.vo.*;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 去重规则业务实现
 * songjuanjuan
 */
@Service
@Slf4j
public class RuleOfSoleServiceImpl implements RuleOfSoleService {

    @Autowired
    SoleRuleConfigMapper soleRuleConfigMapper;

    @Autowired
    CustomerSoleMapper customerSoleMapper;

    @Autowired
    MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    SoleOptLogMapper soleOptLogMapper;

    @Autowired
    private VariableDicMapper variableDicMapper;

    @Autowired
    RuleRedisServiceImpl ruleRedisService;

    @Override
    public PageResultReturn list(int page, int pageSize, String soleName, Integer status,String apiCodes,
                                 String createTimeStart, String createTimeEnd, String updateTimeStart, String updateTimeEnd) {

        if (StringUtils.isNotEmpty(createTimeEnd)){
            createTimeEnd = DateUtils.format(addDay(createTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (StringUtils.isNotEmpty(updateTimeEnd)){
            updateTimeEnd = DateUtils.format(addDay(updateTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (StringUtils.isNotEmpty(soleName) && soleName.contains("_")){
            soleName = soleName.replace("_", "\\_");
        }

        List<SoleRuleVO> soleRuleConfigs = soleRuleConfigMapper.selectList(soleName,status,
                createTimeStart,createTimeEnd,updateTimeStart,updateTimeEnd);
        MarketingUserDetail user = ThreadContextInfo.getUser();
        String apiCodeAuth = "";
        if (user != null) {
            List adminUser = user.getRoleList().stream().filter(marketingRole -> marketingRole.getId() == 1).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(adminUser)) {
                apiCodeAuth = user.getApiCode();
            }
        }
        List<String> apiCodeAuthList = StringUtils.isBlank(apiCodeAuth) ? new ArrayList<>() : Arrays.asList(apiCodeAuth.split(","));
        for (Iterator<SoleRuleVO> iterator = soleRuleConfigs.iterator(); iterator.hasNext(); ) {
            List<String> finalApiCodeList = new ArrayList<>(apiCodeAuthList);
            SoleRuleVO soleRuleConfig = iterator.next();
            //去重字段统计
            soleRuleConfig.setSoleFieldsNum(soleRuleConfig.getSoleFields().split(",").length);
            //使用商户统计
            CustomerSoleExample customerSoleExample = new CustomerSoleExample();
            customerSoleExample.createCriteria().andSoleIdEqualTo(soleRuleConfig.getId())
                    .andIsDelEqualTo(1);
            int count = customerSoleMapper.countByExample(customerSoleExample);
            soleRuleConfig.setCusNum(count);
            //apicode
            List<String> apicodeList = new ArrayList<>();
            Set<String> apicodeSet = new HashSet<>();
            List<CustomerSole> customerSoles = customerSoleMapper.selectByExample(customerSoleExample);
            for (CustomerSole s : customerSoles) {
                MarketingCustomerExample customerExample = new MarketingCustomerExample();
                customerExample.createCriteria().andIdEqualTo(s.getCustomerId());
                List<MarketingCustomer> customers = marketingCustomerMapper.selectByExample(customerExample);
                for (MarketingCustomer c : customers) {
                    apicodeSet.add(c.getApiCode());
                }
            }
            apicodeList.addAll(apicodeSet);
            if (!CollectionUtils.isEmpty(finalApiCodeList)) {
                //取交集
                finalApiCodeList.retainAll(apicodeList);
                if (CollectionUtils.isEmpty(finalApiCodeList)) {
                    iterator.remove();
                } else {
                    soleRuleConfig.setApicodes(finalApiCodeList);
                }
            } else {
                soleRuleConfig.setApicodes(apicodeList);
            }

        }
        if(apiCodes != null && !"".equals(apiCodes)){
            String[] split = apiCodes.split(",");
            soleRuleConfigs = soleRuleConfigs.stream().filter(s -> {
                for (String item : split) {
                    if (s.getApicodes().contains(item)) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
        }
        //对结果进行分页并分装
        PageResultReturn result = new PageResultReturn();
        result.setCurrent(page);
        result.setSize(pageSize);
        if (soleRuleConfigs == null || soleRuleConfigs.size() == 0) {
            result.setRecords(soleRuleConfigs);
            result.setTotal(0);
            return result;
        }

        Integer count = soleRuleConfigs.size(); // 记录总数
        Integer pageCount = 0; // 页数
        if (count % pageSize == 0) {
            pageCount = count / pageSize;
        } else {
            pageCount = count / pageSize + 1;
        }

        int fromIndex = 0; // 开始索引
        int toIndex = 0; // 结束索引

        if (page != pageCount) {
            fromIndex = (page - 1) * pageSize;
            toIndex = fromIndex + pageSize;
        } else {
            fromIndex = (page - 1) * pageSize;
            toIndex = count;
        }

        List pageList = soleRuleConfigs.subList(fromIndex, toIndex);
        result.setRecords(pageList);
        result.setTotal(count);
        return result;
    }

    private Date addDay(String date, Integer addDays, String format) {
        Calendar c = Calendar.getInstance();
        Date time = null;
        try {
            Date endTime = DateUtils.parse(date, format);
            c.setTime(endTime);
            c.add(Calendar.DAY_OF_MONTH, addDays);
            time = c.getTime();
        } catch (ParseException e) {
            log.error("date:{} is error", date, e);
        }
        return time;
    }

    @Override
    public boolean getNameOnly(String soleName,String soleId) {
        if(StringUtils.isBlank(soleName)){
            return false;
        }
        SoleRuleConfigExample example = new SoleRuleConfigExample();
        example.createCriteria().andSoleNameEqualTo(soleName).andIsDelEqualTo(1);
        List<SoleRuleConfig> configs = soleRuleConfigMapper.selectByExample(example);
        if (configs.size() == 0){
            return true;
        }
        if (configs.size()>1){
            log.error("名称为"+soleName+"的规则存在多条！");

            return false;
        }
        for (SoleRuleConfig config:configs){
            if (StringUtils.isNotEmpty(soleId) && soleId.equals(config.getId().toString())){
                return true;
            }
        }
        return false;
    }

    @Override
    public List<MarketingCustomerVO> getCustomer(String search) {
        List<MarketingCustomer> list = marketingCustomerMapper.selectByLike(search);
        //返回id由Long改为string类型
        List<MarketingCustomerVO> vos = list.stream().map(marketingCustomer -> {
            MarketingCustomerVO vo = new MarketingCustomerVO();
            BeanUtils.copyProperties(marketingCustomer, vo);
            vo.setId(marketingCustomer.getId().toString());
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }


    @Override
    public boolean updateStatusById(String id, Integer status, MarketingUserDetail userDetail) {
        SoleRuleConfig config = new SoleRuleConfig();
        config.setId(Long.parseLong(id));
        config.setStatus(status);
        int update = soleRuleConfigMapper.updateByPrimaryKeySelective(config);
        if (update == 1){
            //insert b_sole_opt_log 变更日志表
            //变更内容(规则名称、字段、时间、匹配商户、使用状态)其中匹配商户内容为 简称+apicode 拼接的字符串
            SoleRuleConfig soleRuleConfig = soleRuleConfigMapper.selectByPrimaryKey(Long.parseLong(id));
            SoleOptLog soleOptLog = new SoleOptLog();
            soleOptLog.setSoleId(id);
            soleOptLog.setSoleName(soleRuleConfig.getSoleName());
            soleOptLog.setSoleFields(soleRuleConfig.getSoleFields());
            soleOptLog.setSoleCycleTimes(soleRuleConfig.getSoleCycleTimes());
            soleOptLog.setStatus(soleRuleConfig.getStatus());
            //根据规则id查看其下的匹配商户
            String soleCustomers = getCusBySoleId(Long.parseLong(id));
            soleOptLog.setCustomerInfo(soleCustomers);
            soleOptLog.setOptUserId(String.valueOf(userDetail.getId()));
            soleOptLog.setOptUserName(userDetail.getUserName());
            /*soleOptLog.setOptUserId("sjj");
            soleOptLog.setOptUserName("sjj");*/
            soleOptLog.setUpdateTime(new Date());
            soleOptLog.setCreateTime(new Date());
            soleOptLog.setIsDel(1);
            soleOptLogMapper.insert(soleOptLog);
            List<String> apiCodeBySoleId = getApiCodeBySoleId(Long.parseLong(id));
            apiCodeBySoleId.forEach(t->{ruleRedisService.delSoleConfigRedis(t);});
            return true;
        }else {
            return false;
        }

    }

    @Override
    public PageResultReturn getUpdateRecord(String id,int page,int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<SoleOptLogVO> soleOptLogs = soleOptLogMapper.selectListById(Long.parseLong(id));
        return PageResultReturn.setPageResult(soleOptLogs, page,pageSize);
    }

    /**
     * 校验是否有重复的去重规则
     * @param vo 前端传过来的去重规则数据
     * @return
     */
    public boolean isRuleOfSoleOnly(SoleRuleDetailVO vo){
        Long soleId = null;
        if (StringUtils.isNotEmpty(vo.getSoleId())){
            soleId = Long.parseLong(vo.getSoleId());
        }

        String soleFields = vo.getSoleFields();
        Integer soleCycleTimes = vo.getSoleCycleTimes();
        for (CustUserTypeSelectVO selectVO : vo.getSoleCustom()) {
            Long cid = Long.parseLong(selectVO.getCid());
            Integer allUserType = selectVO.getAllUserType();
            String conditionInfo = selectVO.getConditionInfo().toJSONString();
            int count = 0;
            if(null != allUserType && 1 == allUserType){
                count = soleRuleConfigMapper.getRuleOfSoleOnly(soleId,soleFields,soleCycleTimes,cid,null,allUserType);
            }else{
                count = soleRuleConfigMapper.getRuleOfSoleOnly(soleId,soleFields,soleCycleTimes,cid,conditionInfo,null);
            }
            if(count>0){
                return false;
            }
        }
        return true;
    }


    @Override
    @Transactional
    public ApiResult<Boolean> saveOrUpdate(SoleRuleDetailVO vo, MarketingUserDetail userDetail) {

        //校验规则是否存在
        boolean flag = isRuleOfSoleOnly(vo);
        if (!flag){
            //已存在
            return new ApiResult<Boolean>().success(false, ServiceResultEnum.SUCCESS_3);
        }

        //根据有没有id判断是新增或者变更
        SoleRuleConfig soleRuleConfig = new SoleRuleConfig();
        soleRuleConfig.setSoleName(vo.getSoleName());
        soleRuleConfig.setSoleFields(vo.getSoleFields());
        soleRuleConfig.setSoleCycleTimes(vo.getSoleCycleTimes());
        soleRuleConfig.setIsDel(1);
        soleRuleConfig.setStatus(1);
        soleRuleConfig.setUpdateTime(new Date());

        if (StringUtils.isEmpty(vo.getSoleId())){
            //新增
            //insert b_sole_rule_config
            soleRuleConfig.setCreateTime(new Date());
            int soleId = soleRuleConfigMapper.insert(soleRuleConfig);
            vo.setSoleId(soleRuleConfig.getId().toString());
            if (soleId<=0 || StringUtils.isNull(soleId)){
                log.error("去重规则配置表 保存失败！");
                throw new BusinessException("去重规则配置表 保存失败！");
            }
        }else {
            //变更
            //update b_sole_rule_config
            soleRuleConfig.setId(Long.parseLong(vo.getSoleId()));
            soleRuleConfigMapper.updateById(soleRuleConfig);
            //逻辑删除旧数据 update b_customer_sole
            CustomerSole customerSole = new CustomerSole();
            customerSole.setIsDel(9);
            CustomerSoleExample example = new CustomerSoleExample();
            example.createCriteria().andSoleIdEqualTo(Long.parseLong(vo.getSoleId()));
            customerSoleMapper.updateByExampleSelective(customerSole,example);
        }
        //新增规则列表成功,insert b_customer_sole
        if (vo.getSoleCustom() != null && vo.getSoleCustom().size()>0){
            for(CustUserTypeSelectVO s : vo.getSoleCustom()){
                CustomerSole customerSole = new CustomerSole();
                customerSole.setCustomerId(Long.parseLong(s.getCid()));
                customerSole.setSoleId(Long.parseLong(vo.getSoleId()));
                customerSole.setIsDel(1);
                customerSole.setCreateTime(new Date());
                customerSole.setUpdateTime(new Date());
                Integer allUserType = s.getAllUserType();
                if(null != allUserType){
                    customerSole.setAllUserType(allUserType);
                }
                JSONObject conditionInfo = s.getConditionInfo();
                if(null != conditionInfo){
                    String conditionInfoString = conditionInfo.toJSONString();
                    customerSole.setConditionInfo(conditionInfoString);
                    RuleConditionVo conditionVo = JSON.parseObject(conditionInfoString, new TypeReference<RuleConditionVo>() {
                    }.getType());
                    List<RuleConditionFactorVo> operationFactorList = conditionVo.getOperationFactor();
                    if(null != operationFactorList){
                        customerSole.setUserTypeCount(operationFactorList.size());
                    }
                }
                customerSoleMapper.insertSelective(customerSole);
                ruleRedisService.delSoleConfigRedis(s.getApiCode());
            }
        }
        //insert b_sole_opt_log 变更日志表
        //变更内容(规则名称、字段、时间、匹配商户、使用状态)其中匹配商户内容为 简称+apicode 拼接的字符串
        SoleOptLog soleOptLog = new SoleOptLog();
        soleOptLog.setSoleId(vo.getSoleId());
        soleOptLog.setSoleName(vo.getSoleName());
        soleOptLog.setSoleFields(vo.getSoleFields());
        soleOptLog.setSoleCycleTimes(vo.getSoleCycleTimes());
        soleOptLog.setStatus(1);
        //根据规则id查看其下的匹配商户
        String soleCustomers = getCusBySoleId(Long.parseLong(vo.getSoleId()));
        soleOptLog.setCustomerInfo(soleCustomers);
        soleOptLog.setOptUserId(String.valueOf(userDetail.getId()));
        soleOptLog.setOptUserName(userDetail.getUserName());
       /* soleOptLog.setOptUserId("sjj");
        soleOptLog.setOptUserName("sjj");*/
        soleOptLog.setUpdateTime(new Date());
        soleOptLog.setCreateTime(new Date());
        soleOptLog.setIsDel(1);
        soleOptLogMapper.insert(soleOptLog);

        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public SoleRuleDetailVO getSoleById(String id) {
        SoleRuleDetailVO vo = new SoleRuleDetailVO();
        SoleRuleConfig soleRuleConfig = soleRuleConfigMapper.selectByPrimaryKey(Long.parseLong(id));
        BeanUtils.copyProperties(soleRuleConfig, vo);
        vo.setSoleId(soleRuleConfig.getId().toString());

        List<CustUserTypeSelectVO> soleCustomVO = new ArrayList<>();
        CustomerSoleExample example = new CustomerSoleExample();
        example.createCriteria().andSoleIdEqualTo(Long.parseLong(id)).andIsDelEqualTo(1);
        List<CustomerSole> customerSoles = customerSoleMapper.selectByExample(example);
        for (CustomerSole sole : customerSoles){
            MarketingCustomer customer = marketingCustomerMapper.selectByPrimaryKey(sole.getCustomerId());
            String cid = customer.getCid();
            String apiCode = customer.getApiCode();
            CustUserTypeSelectVO selectVO = new CustUserTypeSelectVO();
            selectVO.setCid(sole.getCustomerId().toString());
            selectVO.setAllUserType(sole.getAllUserType());
            if(1 == sole.getAllUserType()){
                RuleConditionVo conditionVo = new RuleConditionVo();
                conditionVo.setLogicalOperation("or");
                List<RuleConditionFactorVo> operationFactorList = new ArrayList<>();
                VariableDicExample variableDicexample = new VariableDicExample();
                variableDicexample.createCriteria().andCidEqualTo(cid)
                        .andApiCodeEqualTo(apiCode)
                        .andIsDelEqualTo(1);
                List<VariableDic> variableDics = variableDicMapper.selectByExample(variableDicexample);
                if (variableDics !=null && variableDics.size()>0) {
                    for (int i = 0; i < variableDics.size(); i++) {
                        VariableDic variableDic = variableDics.get(i);
                        RuleConditionFactorVo ruleConditionFactorVo = new RuleConditionFactorVo();
                        ruleConditionFactorVo.setFieldName("userType");
                        ruleConditionFactorVo.setFieldValue(variableDic.getFieldValue());
                        ruleConditionFactorVo.setOperation("=");
                        operationFactorList.add(ruleConditionFactorVo);
                    }
                }
                conditionVo.setOperationFactor(operationFactorList);
                selectVO.setConditionInfo(JSON.parseObject(JSON.toJSONString(conditionVo)));
            }else{
                selectVO.setConditionInfo(JSON.parseObject(sole.getConditionInfo()));
            }
            selectVO.setApiCode(apiCode !=null? apiCode :"");
            selectVO.setName(customer.getName()!=null?customer.getName():"");
            selectVO.setShortName(customer.getShortName()!=null?customer.getShortName():"");
            soleCustomVO.add(selectVO);
        }
        vo.setSoleCustom(soleCustomVO);
        return vo;
    }

    @Override
    public List<Map> getUserByCus(List<MarketingCustomerVO> customerVOs) {
        List list = new ArrayList();
        String[] type = {"usertype","grouptype"};
        for(MarketingCustomerVO customerVO : customerVOs){
            Map map = new HashMap();
            map.put("shortName",customerVO.getShortName());
            map.put("cid",customerVO.getCid());
            map.put("id",customerVO.getId());
            map.put("apiCode",customerVO.getApiCode());
            map.put("name",customerVO.getName());
            map.put("cusCollapseVal",type);

            //场景列表
            Map usertypeMap = new HashMap();
            usertypeMap.put("name","运营场景");
            usertypeMap.put("type","usertype");
            usertypeMap.put("indeterminate",false);
            usertypeMap.put("checkAll",false);
            usertypeMap.put("checkAllGroup",new ArrayList<>());
            List<VariableDicSelectVO> dicSelectVOS = new ArrayList<>();
            VariableDicExample example = new VariableDicExample();
            example.createCriteria().andCidEqualTo(customerVO.getCid())
                    .andApiCodeEqualTo(customerVO.getApiCode())
                    .andIsDelEqualTo(1);
            List<VariableDic> variableDics = variableDicMapper.selectByExample(example);
            if (variableDics !=null && variableDics.size()>0) {
                dicSelectVOS = variableDics.stream().map(v -> new VariableDicSelectVO(
                        v.getFieldName(), v.getFieldValue(), v.getFieldDesc())).collect(Collectors.toList());
            }
            usertypeMap.put("list",dicSelectVOS);

            map.put("usertype",usertypeMap);
            list.add(map);
        }

        return list;
    }


    /**
     * 商户当前规则下的场景
     * @param soleId
     * @param customerId
     * @return
     */
    public List<CustomerSole> getUserTypeByCus(Long soleId, Long customerId) {
        CustomerSoleExample example = new CustomerSoleExample();
        example.createCriteria().andIsDelEqualTo(1)
                .andSoleIdEqualTo(soleId)
                .andCustomerIdEqualTo(customerId);
        List<CustomerSole> customerSoles = customerSoleMapper.selectByExample(example);
        return customerSoles;
    }

    /**
     * 根据规则id查看其下的匹配商户,用在变更记录表 匹配商户字段
     * @return
     */
    public String getCusBySoleId(Long soleId){
        CustomerSoleExample example = new CustomerSoleExample();
        example.createCriteria().andSoleIdEqualTo(soleId).andIsDelEqualTo(1);
        List<CustomerSole> customerSoles = customerSoleMapper.selectByExample(example);
        StringBuilder cus = new StringBuilder();
        if (customerSoles.size()==0){
            cus.append("未匹配");
            return cus.toString();
        }
        for (CustomerSole sole : customerSoles){
            MarketingCustomer marketingCus = marketingCustomerMapper.selectByPrimaryKey(sole.getCustomerId());
            cus.append(marketingCus.getShortName()).append(marketingCus.getApiCode()).append(",");
        }
        return cus.deleteCharAt(cus.length()-1).toString();
    }


    public List<String> getApiCodeBySoleId(Long soleId){
        ArrayList<String> apiCodes = new ArrayList<>();
        CustomerSoleExample example = new CustomerSoleExample();
        example.createCriteria().andSoleIdEqualTo(soleId).andIsDelEqualTo(1);
        List<CustomerSole> customerSoles = customerSoleMapper.selectByExample(example);
        StringBuilder cus = new StringBuilder();
        if (customerSoles.size()==0){
            return apiCodes;
        }
        List<Long> customerIds = customerSoles.stream().map(t -> t.getCustomerId()).collect(Collectors.toList());
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andIdIn(customerIds);
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        for (MarketingCustomer customer : marketingCustomers){
            apiCodes.add(customer.getApiCode());
        }
        return apiCodes;
    }
}
