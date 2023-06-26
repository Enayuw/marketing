package com.br.marketing.service.Impl.yixin;

import cn.hutool.core.collection.CollectionUtil;
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
     * @return 是否剔除，true 剔除  false  不剔除
     */
    public void action_A(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleFirst(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSecond(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleThird(marketingTransferSyncUserList);
        }

    }

    /**
     * 情况 b 判断剔除
     *
     * @return 是否剔除，true 剔除  false  不剔除
     */
    public void action_B(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleFifth(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSecond(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSixth(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleThird(marketingTransferSyncUserList);
        }
    }

    /**
     * 情况 c~i 判断剔除
     *
     * @return 是否剔除，true 剔除  false  不剔除
     */
    public void action_C_to_I(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSecond(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSixth(marketingTransferSyncUserList);
        }
        if(CollectionUtils.isNotEmpty(marketingTransferSyncUserList)){
            yiXinProcessGetBaseExcludeRuleDataService.excludeRuleThird(marketingTransferSyncUserList);
        }

    }
}
