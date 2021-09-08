package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.common.util.DateUtils;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.RuleOfSoleService;
import com.br.marketing.vo.*;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Override
    public PageResultReturn list(SoleRuleSearchDTO dto, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        if (StringUtils.isNotEmpty(dto.getCreateTimeEnd())){
            dto.setCreateTimeEnd(DateUtils.format(addDay(dto.getCreateTimeEnd(), 1, "yyyy-MM-dd"), "yyyy-MM-dd"));
        }
        if (StringUtils.isNotEmpty(dto.getUpdateTimeEnd())){
            dto.setUpdateTimeEnd(DateUtils.format(addDay(dto.getUpdateTimeEnd(), 1, "yyyy-MM-dd"), "yyyy-MM-dd"));
        }

        List<SoleRuleConfig> soleRuleConfigs = soleRuleConfigMapper.selectList(dto);
        List<SoleRuleVO> soleRuleVos = soleRuleConfigs.stream().map(soleRuleConfig -> {
            SoleRuleVO vo = new SoleRuleVO();
            BeanUtils.copyProperties(soleRuleConfig,vo);
            //id类型转换
            vo.setId(soleRuleConfig.getId().toString());
            //去重字段统计
            vo.setSoleFieldsNum(soleRuleConfig.getSoleFields().split(",").length);
            //使用商户统计
            CustomerSoleExample customerSoleExample = new CustomerSoleExample();
            customerSoleExample.createCriteria().andSoleIdEqualTo(soleRuleConfig.getId())
                    .andIsDelEqualTo(1);
            int count = customerSoleMapper.countByExample(customerSoleExample);
            vo.setCusNum(count);
            vo.setCreateTime(DateUtils.format(soleRuleConfig.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
            vo.setUpdateTime(DateUtils.format(soleRuleConfig.getUpdateTime(),"yyyy-MM-dd HH:mm:ss"));
            return vo;
            }).collect(Collectors.toList());
        return PageResultReturn.setPageResult(soleRuleVos, page);
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
    public boolean getCusUserType(String soleId, String customerId) {
        CustomerSoleExample customerSoleExample = new CustomerSoleExample();
        customerSoleExample.createCriteria()
                .andCustomerIdEqualTo(Long.parseLong(customerId))
                .andIsDelEqualTo(1);
        List<CustomerSole> customerSoles = customerSoleMapper.selectByExample(customerSoleExample);
        if(customerSoles.size() == 0){
            return true;
        }
        if(customerSoles.size()>=1){
            for (CustomerSole c:customerSoles){
                if(!soleId.equals(c.getSoleId().toString())){
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean updateStatusById(String id, Integer status) {
        SoleRuleConfig config = new SoleRuleConfig();
        config.setId(Long.parseLong(id));
        config.setStatus(status);
        int update = soleRuleConfigMapper.updateByPrimaryKeySelective(config);
        if (update == 1){
            return true;
        }else {
            return false;
        }

    }

    @Override
    public List<SoleOptLogVO> getUpdateRecord(String id) {
        List<SoleOptLogVO> logVOS = new ArrayList<>();
        SoleOptLogExample example = new SoleOptLogExample();
        example.createCriteria().andIsDelEqualTo(1).andSoleIdEqualTo(id);
        List<SoleOptLog> soleOptLogs = soleOptLogMapper.selectByExample(example);
        if(soleOptLogs != null && soleOptLogs.size()>0){
            for (SoleOptLog log : soleOptLogs){
                SoleOptLogVO logVO = new SoleOptLogVO();
                BeanUtils.copyProperties(log, logVO);
                logVOS.add(logVO);
            }
        }

        return logVOS;
    }

    @Override
    @Transactional
    public boolean saveOrUpdate(SoleRuleDetailVO vo,UserDetail userDetail) {
        //根据有没有id判断是新增或者变更
        SoleRuleConfig soleRuleConfig = new SoleRuleConfig();
        soleRuleConfig.setSoleName(vo.getSoleName());
        soleRuleConfig.setSoleFields(vo.getSoleFields());
        soleRuleConfig.setSoleCycleTimes(vo.getSoleCycleTimes());
        soleRuleConfig.setStatus(2);
        soleRuleConfig.setIsDel(1);
        soleRuleConfig.setCreateTime(new Date());
        soleRuleConfig.setUpdateTime(new Date());
        if (StringUtils.isEmpty(vo.getSoleId())){
            //新增
            //insert b_sole_rule_config
            soleRuleConfig.setUpdateTime(new Date());
            int soleId = soleRuleConfigMapper.insert(soleRuleConfig);
            vo.setSoleId(soleRuleConfig.getId().toString());
            if (soleId<=0 || StringUtils.isNull(soleId)){
                log.error("去重规则配置表 保存失败！");
                throw new BusinessException("去重规则配置表 保存失败！");
            }
        }else {
            //变更
            //insert b_sole_opt_log
            SoleOptLog soleOptLog = new SoleOptLog();
            soleOptLog.setSoleId(vo.getSoleId());
            //变更内容(规则名称、字段、时间、匹配商户、使用状态)其中匹配商户内容为 简称+apicode 拼接的字符串
            SoleRuleConfig old = soleRuleConfigMapper.selectByPrimaryKey(Long.parseLong(vo.getSoleId()));
            soleOptLog.setSoleName(old.getSoleName());
            soleOptLog.setSoleFields(old.getSoleFields());
            soleOptLog.setSoleCycleTimes(old.getSoleCycleTimes());
            soleOptLog.setStatus(old.getStatus());
            //根据规则id查看其下的匹配商户
            String soleCustomers = getCusBySoleId(old.getId());
            soleOptLog.setCustomerInfo(soleCustomers);
            soleOptLog.setOptUserId(userDetail.getUserId());
            soleOptLog.setOptUserName(userDetail.getUsername());
            /*soleOptLog.setOptUserId("测试用户id");
            soleOptLog.setOptUserName("测试用户名称");*/
            soleOptLog.setUpdateTime(new Date());
            soleOptLog.setCreateTime(new Date());
            soleOptLog.setIsDel(1);
            soleOptLogMapper.insertSelective(soleOptLog);
            //update b_sole_rule_config
            soleRuleConfig.setId(Long.parseLong(vo.getSoleId()));
            soleRuleConfig.setUpdateTime(new Date());
            soleRuleConfigMapper.updateByPrimaryKeySelective(soleRuleConfig);
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
                customerSole.setConditionInfo(s.getConditionInfo().toJSONString());
                customerSoleMapper.insertSelective(customerSole);
            }
        }
        return true;
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
            CustUserTypeSelectVO selectVO = new CustUserTypeSelectVO();
            selectVO.setCid(sole.getCustomerId().toString());
            selectVO.setConditionInfo(JSON.parseObject(sole.getConditionInfo()));
            MarketingCustomer customer = marketingCustomerMapper.selectByPrimaryKey(sole.getCustomerId());
            selectVO.setApiCode(customer.getApiCode()!=null?customer.getApiCode():"");
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
     * 根据规则id查看其下的匹配商户
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

}
