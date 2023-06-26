package com.br.marketing.service.Impl.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.extern.slf4j.Slf4j;
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
    public List<MarketingTransferSyncUser> action_A(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        List<MarketingTransferSyncUser> marketingTransferSyncUsers = yiXinProcessGetBaseExcludeRuleDataService.excludeRuleFirst(marketingTransferSyncUserList);
        List<MarketingTransferSyncUser> marketingTransferSyncUsers1 = yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSecond(marketingTransferSyncUsers);
        List<MarketingTransferSyncUser> marketingTransferSyncUsers2 = yiXinProcessGetBaseExcludeRuleDataService.excludeRuleThird(marketingTransferSyncUsers1);
        return yiXinProcessGetBaseExcludeRuleDataService.excludeRuleFourth(marketingTransferSyncUsers2);

    }

    /**
     * 情况 b 判断剔除
     *
     * @return 是否剔除，true 剔除  false  不剔除
     */
    public List<MarketingTransferSyncUser> action_B(List<MarketingTransferSyncUser> marketingTransferSyncUser) {
        List<MarketingTransferSyncUser> marketingTransferSyncUsers = yiXinProcessGetBaseExcludeRuleDataService.excludeRuleFifth(marketingTransferSyncUser);
        List<MarketingTransferSyncUser> marketingTransferSyncUsers1 = yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSecond(marketingTransferSyncUsers);
        List<MarketingTransferSyncUser> marketingTransferSyncUsers2 = yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSixth(marketingTransferSyncUsers1);
        List<MarketingTransferSyncUser> marketingTransferSyncUsers3 = yiXinProcessGetBaseExcludeRuleDataService.excludeRuleThird(marketingTransferSyncUsers2);
        return marketingTransferSyncUsers3;
    }

    /**
     * 情况 c~i 判断剔除
     *
     * @return 是否剔除，true 剔除  false  不剔除
     */
    public List<MarketingTransferSyncUser> action_C_to_I(List<MarketingTransferSyncUser> marketingTransferSyncUser) {
        List<MarketingTransferSyncUser> marketingTransferSyncUsers = yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSecond(marketingTransferSyncUser);
        List<MarketingTransferSyncUser> marketingTransferSyncUsers1 = yiXinProcessGetBaseExcludeRuleDataService.excludeRuleSixth(marketingTransferSyncUsers);
        List<MarketingTransferSyncUser> marketingTransferSyncUsers2 = yiXinProcessGetBaseExcludeRuleDataService.excludeRuleThird(marketingTransferSyncUsers1);
        return marketingTransferSyncUsers2;

    }
}
