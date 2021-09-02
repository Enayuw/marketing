package com.br.marketing.service.Impl;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerSoleMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.SoleRuleConfigMapper;
import com.br.marketing.service.RuleOfSoleService;
import com.br.marketing.vo.SoleOptLogVO;
import com.br.marketing.vo.SoleRuleVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        List<SoleRuleVO> soleRuleVos = new ArrayList<>();
        CustomerSoleExample customerSoleExample;
        for(SoleRuleConfig single:soleRuleConfigs){
            SoleRuleVO vo = new SoleRuleVO();
            vo.setId(single.getId());
            vo.setSoleName(single.getSoleName());
            vo.setSoleFields(single.getSoleFields());
            vo.setSoleFieldsNum(single.getSoleFields().split(",").length);//去重字段统计
            if(StringUtils.isNotEmpty(single.getSoleCycleTimes())){
                vo.setSoleCycleTimes(single.getSoleCycleTimes());
            }
            //使用商户统计
            customerSoleExample = new CustomerSoleExample();
            customerSoleExample.createCriteria().andSoleIdEqualTo(single.getId())
                    .andIsDelEqualTo(1);
            int count = customerSoleMapper.countByExample(customerSoleExample);
            vo.setCusNum(count);
            vo.setStatus(single.getStatus());
            vo.setCreateTime(single.getCreateTime());
            vo.setUpdateTime(single.getUpdateTime());
            soleRuleVos.add(vo);
        }

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
    public List<MarketingCustomer> getCustomer(String search) {
        List<MarketingCustomer> list = marketingCustomerMapper.selectByLike(search);
        return list;
    }

    @Override
    public boolean getCusUserType(Long soleId, Long customerId) {
        CustomerSoleExample customerSoleExample = new CustomerSoleExample();
        customerSoleExample.createCriteria()
                .andCustomerIdEqualTo(customerId)
                .andIsDelEqualTo(1);
        List<CustomerSole> customerSoles = customerSoleMapper.selectByExample(customerSoleExample);
        if(customerSoles.size()>1){
            log.error("customerId为"+customerId+"的商户匹配了多条规则！");
            return false;
        }

        for (CustomerSole c:customerSoles){
            if(c.getSoleId() == soleId){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateStatusById(Long id, Integer status) {
        SoleRuleConfig config = new SoleRuleConfig();
        config.setId(id);
        config.setStatus(status);
        int update = soleRuleConfigMapper.updateByPrimaryKeySelective(config);
        if (update == 1){
            return true;
        }else {
            return false;
        }

    }

    @Override
    public List<SoleOptLogVO> getUpdateRecord(Long id) {




        return null;
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
