package com.br.marketing.service.Impl.YiXin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 宜信推送决策情况 a~i 组装剔除逻辑
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:27
 */
@Component
public class YiXinProcessExcludeRuleData {

    @Resource
    private YiXinProcessGetBaseExcludeRuleDataService yiXinProcessGetBaseExcludeRuleDataService;

    /**
     * 情况 a 判断剔除
     * @return
     */
    public Boolean  action_A(MarketingTransferSyncUser marketingTransferSyncUser){
        return null;
    }

    /**
     * 情况 b 判断剔除
     * @return
     */
    public Boolean action_B(MarketingTransferSyncUser marketingTransferSyncUser){
        return null;
    }

    /**
     * 情况 c~i 判断剔除
     * @return
     */
    public Boolean action_C_to_I(MarketingTransferSyncUser marketingTransferSyncUser){
        return null;
    }
}
