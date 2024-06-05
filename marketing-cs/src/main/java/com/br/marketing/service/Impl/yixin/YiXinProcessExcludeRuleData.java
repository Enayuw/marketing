package com.br.marketing.service.Impl.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 宜信推送决策情况 a~i 组装剔除逻辑
 *
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:27
 */
@Service
@Slf4j
public class YiXinProcessExcludeRuleData {

    @Resource
    private YiXinProcessGetBaseExcludeRuleDataService yiXinProcessGetBaseExcludeRuleDataService;

    /**
     * 情况 a 判断剔除
     *
     */
    public void excludeActionA(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleFirst(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSecond(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSixth(marketingTransferSyncUserList);
        }
    }

    /**
     * 情况 b 判断剔除
     *
     */
    public void excludeActionB(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleFifth(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSecond(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSixth(marketingTransferSyncUserList);
        }
    }

    /**
     * 情况 c~i 判断剔除
     *
     */
    public void excludeActionCtoI(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSecond(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSixth(marketingTransferSyncUserList);
        }
    }

    /**
     * 情况 l 判断剔除
     *
     */
    public void excludeActionL(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSecond(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSixth(marketingTransferSyncUserList);
        }
        log.warn("宜信推送决策,情况L剔除后数据量级:{}", marketingTransferSyncUserList.size());
    }
}
