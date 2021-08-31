package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.mapper.CustomerSoleMapper;
import com.br.marketing.mapper.ScoreRuleConfigMapper;
import com.br.marketing.mapper.SoleRuleConfigMapper;
import com.br.marketing.service.IScoreConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreConfigServiceImpl implements IScoreConfigService {

    @Autowired
    ScoreRuleConfigMapper scoreRuleConfigMapper;

    @Autowired
    SoleRuleConfigMapper soleRuleConfigMapper;

    @Autowired
    CustomerSoleMapper customerSoleMapper;

    @Autowired
    CustomerMapper customerMapper;

    @Override
    public Result<SoleRuleConfig> getSoleConfig(String apiCode) {

        Customer customerByApiCode = customerMapper.getCustomerByApiCode(apiCode);

        CustomerSoleExample customerSoleExample= new CustomerSoleExample();
        customerSoleExample.createCriteria()
                .andCustomerIdEqualTo(customerByApiCode.getId())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<CustomerSole> customerSoles = customerSoleMapper.selectByExample(customerSoleExample);
        if(customerSoles.size()<=0){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该用户没有匹配的去重规则");
        }
        if(customerSoles.size()>1){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该用户匹配出多条去重规则");
        }
        CustomerSole customerSole = customerSoles.get(0);

        SoleRuleConfigExample soleRuleConfigExample = new SoleRuleConfigExample();
        soleRuleConfigExample.createCriteria()
                .andIdEqualTo(customerSole.getSoleId())
                .andStatusEqualTo(Constants.STATUS_START)
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<SoleRuleConfig> soleRuleConfigs = soleRuleConfigMapper.selectByExample(soleRuleConfigExample);
        if(soleRuleConfigs.size()<=0){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该用户的去重规则是否已失效");
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(soleRuleConfigs.get(0));
    }


}
