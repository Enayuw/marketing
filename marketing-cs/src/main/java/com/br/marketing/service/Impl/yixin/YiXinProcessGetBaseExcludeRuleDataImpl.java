package com.br.marketing.service.Impl.yixin;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
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
    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Override
    public Boolean excludeRuleFirst(MarketingTransferSyncUser marketingTransferSyncUser) {
        // todo
        if (StringUtils.isEmpty(marketingTransferSyncUser.getCustNum())) {
            return true;
        }
        // 取apicode
        String apiCode = "";
        String tcId = tableCreateService.getTcId(apiCode);
        // 获取前一天的日期
        String today = LocalDate.now().toString();
        int count = marketingTransferSyncUserMapper.get_ExcludeRuleFirst_YxTransferByApiCode_A(tcId, apiCode,
                today, marketingTransferSyncUser.getCustNum());
        return count > 0;
    }

    @Override
    public Boolean excludeRuleSecond(MarketingTransferSyncUser marketingTransferSyncUser) {
        // todo
        return "0".equals(marketingTransferSyncUser.getCaseEffective());
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
        // todo
        return null;
    }

    @Override
    public Boolean excludeRuleSixth(MarketingTransferSyncUser marketingTransferSyncUser) {
        return null;
    }
}
