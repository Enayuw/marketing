package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.IDynamicSqlService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class DynamicParameterServiceImpl {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    /**
     * 获取分页的数量
     * @param key
     * @return
     */
    public Integer getPageSize(String key){
        if (marketingCommonConfig.getDynamicPageSize() != null && marketingCommonConfig.getDynamicPageSize().get(key) != null) {
            try {
                Integer num = Integer.valueOf(marketingCommonConfig.getDynamicPageSize().get(key));
                return num;
            }catch (Exception ex){
                log.error(String.format("获取分页错误:【%s】",key),ex);
            }
        }
        return 2000;
    }
}
