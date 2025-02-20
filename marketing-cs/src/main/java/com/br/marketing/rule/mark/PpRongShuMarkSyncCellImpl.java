package com.br.marketing.rule.mark;

import com.br.common.log.AlertLog;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description pp榕树打标同步sha256和log手机号
 * @Author hong.chen
 * @CreateTime 2025/02/19
 */
@Service
@Slf4j
public class PpRongShuMarkSyncCellImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Autowired
    FlagDataMapper flagDataMapper;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingSyncUser) {
            MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
            String encType = marketingCommonConfig.getAutoSyncCellToFlagDataEncTypeConfig().get(syncUser.getApiCode());
            String cellMd5 = syncUser.getCellMd5();
            String cellSha256 = syncUser.getCellSha256();
            String cellLog = syncUser.getCell();
            String apiCode = syncUser.getApiCode();
            try {
                flagDataMapper.updateByDynamicEncCell(cellMd5, cellSha256, cellLog, apiCode, encType);
            } catch (Exception e) {
                String subject = "pp榕树打标更新sha256和log手机号异常,cellMd5:" + cellMd5;
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage()
                        , subject), e);
            }
            return true;
        }

        return false;
    }

    @Override
    public String label() {
        return null;
    }

    @Override
    public Integer dataDirection() {
        return null;
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
