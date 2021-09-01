package com.br.marketing.service.Impl;

import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.entity.CustomerSoleExample;
import com.br.marketing.entity.SoleRuleConfig;
import com.br.marketing.entity.SoleRuleConfigExample;
import com.br.marketing.mapper.CustomerSoleMapper;
import com.br.marketing.mapper.SoleRuleConfigMapper;
import com.br.marketing.service.RuleOfSoleService;
import com.br.marketing.vo.SoleRuleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RuleOfSoleServiceImpl implements RuleOfSoleService {

    @Autowired
    SoleRuleConfigMapper soleRuleConfigMapper;

    @Autowired
    CustomerSoleMapper customerSoleMapper;


    @Override
    public Result<List<SoleRuleVO>> list(SoleRuleSearchDTO dto) {
        SoleRuleConfigExample example = new SoleRuleConfigExample();
        example.createCriteria().andSoleNameLike(dto.getSoleName())
                .andStatusEqualTo(dto.getStatus())
                .andCreateTimeBetween(dto.getCreateTimeStart(),dto.getCreateTimeEnd())
                .andUpdateTimeBetween(dto.getUpdateTimeStart(),dto.getUpdateTimeEnd())
                .andIsDelEqualTo(1);
        List<SoleRuleConfig> soleRuleConfigs = soleRuleConfigMapper.selectByExample(example);

        List<SoleRuleVO> soleRuleVos = new ArrayList<>();
        CustomerSoleExample customerSoleExample;
        for(SoleRuleConfig single:soleRuleConfigs){
            SoleRuleVO vo = new SoleRuleVO();
            vo.setId(single.getId());
            vo.setSoleName(single.getSoleName());
            vo.setSoleFields(single.getSoleFields());
            vo.setSoleFieldsNum(single.getSoleFields().split(",").length);//去重字段统计
            vo.setSoleCycleTimes(single.getSoleCycleTimes());
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
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(soleRuleVos);
    }
}
