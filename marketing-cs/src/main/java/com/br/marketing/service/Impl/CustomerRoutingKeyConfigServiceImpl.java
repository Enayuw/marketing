package com.br.marketing.service.Impl;

import com.br.marketing.entity.CustomerRoutingKeyConfig;
import com.br.marketing.mapper.CustomerRoutingKeyConfigMapper;
import com.br.marketing.service.CustomerRoutingKeyConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description CustomerRoutingKeyConfigServiceImpl
 * @Author hong.chen
 * @CreateTime 2024/03/05
 */
@Service
public class CustomerRoutingKeyConfigServiceImpl implements CustomerRoutingKeyConfigService {
    @Resource
    private CustomerRoutingKeyConfigMapper configMapper;

    @Override
    public CustomerRoutingKeyConfig getCustomerRoutingKeyConfig(String apiCodeJointBizType) {
        String[] split = apiCodeJointBizType.split(",");
        String apiCode = split[0];
        String bizType = split[1];
        return configMapper.getCustomerRoutingKeyConfig(apiCode, bizType);
    }
}
