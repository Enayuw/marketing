package com.br.marketing.rule.huatai;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.common.CommonRuleLabelEnum;
import com.br.marketing.rule.huatai.dto.HuaTaiTransferAssembleDTO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class HuaTaiSyncToTransferPatLoanRule implements AssembleData<HuaTaiTransferAssembleDTO> {

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public HuaTaiTransferAssembleDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        HuaTaiTransferAssembleDTO dto = new HuaTaiTransferAssembleDTO();
        dto.setSyncUser(syncUser);
        return dto;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        if (!(transmitFact instanceof MarketingSyncUser)) {
            return false;
        }
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        List<String> userTypes = marketingCommonConfig.getHuaTaiSyncToTransferUserTypeList();
        return  !CollectionUtils.isEmpty(userTypes) && userTypes.contains(syncUser.getUserType());
    }

    @Override
    public String label() {
        return CommonRuleLabelEnum.HUATAI_SYNC_TO_TRANSFER.getCode();
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.HUATAI_SYNC_TO_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
