package com.br.marketing.check.beanhadler;

import com.br.marketing.entity.MarketingDataFileConfig;
import com.br.marketing.service.IFileToMarketingRuleService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 文件数据清洗具体处理类获取
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-24
 */
@Component
public class DataCleanFactory {

    @Resource
    private SpringContextHandler springContextHandler;

    public IFileToMarketingRuleService getFileToMarketingRuleService(MarketingDataFileConfig config) {
        Map<String, IFileToMarketingRuleService> beansOfRule = springContextHandler.getBeansOfType(IFileToMarketingRuleService.class);
        IFileToMarketingRuleService iFileToMarketingRuleService = beansOfRule.get(config.getServiceName());
        return iFileToMarketingRuleService;
    }

}
