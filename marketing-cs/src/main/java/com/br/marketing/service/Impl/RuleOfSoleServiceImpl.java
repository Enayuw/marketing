package com.br.marketing.service.Impl;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerSoleMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.SoleRuleConfigMapper;
import com.br.marketing.service.RuleOfSoleService;
import com.br.marketing.vo.MarketingCustomerVO;
import com.br.marketing.vo.SoleOptLogVO;
import com.br.marketing.vo.SoleRuleDetailVO;
import com.br.marketing.vo.SoleRuleVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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



    @Override
    public PageResultReturn list(SoleRuleSearchDTO dto, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
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
            return vo;
            }).collect(Collectors.toList());
        return PageResultReturn.setPageResult(soleRuleVos, page);
    }

    @Override
    public boolean getNameOnly(String soleName) {
        SoleRuleConfigExample example = new SoleRuleConfigExample();
        example.createCriteria().andSoleNameEqualTo(soleName).andIsDelEqualTo(1);
        int count = soleRuleConfigMapper.countByExample(example);
        if (count<=0){
            return true;
        }else {
            return false;
        }
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
        if(customerSoles.size()>1){
            log.error("customerId为"+customerId+"的商户匹配了多条规则！");
            return false;
        }

        for (CustomerSole c:customerSoles){
            if(soleId.equals(c.getSoleId().toString())){
                return true;
            }
        }
        return false;
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



        return null;
    }

    @Override
    public boolean saveOrUpdate(SoleRuleDetailVO vo) {
        //根据有没有id判断是新增或者变更
        if (StringUtils.isEmpty(vo.getSoleId())){
            //新增
        }else {
            //变更
            //变更的时候还需要 往日志表添加信息，添加此条更改之前的规则记录
            //存储变更记录-->表里字段是 varchar类型，需要转换一下
        }

        return false;
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
}
