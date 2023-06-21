package com.br.marketing.service.Impl.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;

/**
 * 宜信基础剔除规则实现类
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:33
 */
@Service
@Slf4j
public class YiXinProcessGetBaseExcludeRuleDataImpl implements YiXinProcessGetBaseExcludeRuleDataService{

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public Boolean excludeRuleFirst(MarketingTransferSyncUser marketingTransferSyncUser) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        // 获取当天的日期yyyy-MM-dd
        String today = LocalDate.now().toString();
        int count = marketingTransferSyncUserMapper.get_ExcludeRuleFirst_YxTransferByApiCode(marketingTransferSyncUser.gettCid(), apiCode,
                today, marketingTransferSyncUser.getCustNum());
        return count > 0;
    }

    @Override
    public Boolean excludeRuleSecond(MarketingTransferSyncUser marketingTransferSyncUser) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        String tcId = marketingTransferSyncUser.gettCid();
        int count = marketingTransferSyncUserMapper.get_ExcludeRuleSecond_YxTransferByApiCode(tcId, apiCode, marketingTransferSyncUser.getCustNum());
        return count > 0;
    }

    @Override
    public Boolean excludeRuleThird(MarketingTransferSyncUser marketingTransferSyncUser) {
        return null;
    }

    @Override
    public Boolean excludeRuleFourth(MarketingTransferSyncUser marketingTransferSyncUser) {
        return null;
    }

    @Override
    public Boolean excludeRuleFifth(MarketingTransferSyncUser marketingTransferSyncUser) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        String tcId = marketingTransferSyncUser.gettCid();
        // 获取当天的日期yyyy-MM-dd
        String today = LocalDate.now().toString();
        // 获取30天之前的日期yyyy-MM-dd
        String minus30Days = LocalDate.now().minusDays(30).toString();
        int count = marketingTransferSyncUserMapper.get_ExcludeRuleFifth_YxTransferByApiCode(tcId, apiCode,
                today, minus30Days, marketingTransferSyncUser.getCustNum());
        return count > 0;
    }

    @Override
    public Boolean excludeRuleSixth(MarketingTransferSyncUser marketingTransferSyncUser) {
        return null;
    }
}
