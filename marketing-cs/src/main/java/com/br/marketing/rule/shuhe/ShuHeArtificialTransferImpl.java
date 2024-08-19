package com.br.marketing.rule.shuhe;

import com.br.marketing.client.dassservice.input.transfer.DassAssembleTransferDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeRuleCollectDataImpl;
import com.br.marketing.dto.shuhe.strategy.BaseUserType;
import com.br.marketing.dto.shuhe.strategy.CuFuJie;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 数禾转化数据推送电销转化接口 业务
 *
 * @author lizhen
 * @dateTime 2022/4/27 14:45
 */
@Service
@Slf4j
public class ShuHeArtificialTransferImpl implements AssembleData<DassAssembleTransferDataDTO> {

    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;
    @Resource
    private DataLoadingHandlerService handlerService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Override
    public DassAssembleTransferDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
        DassAssembleTransferDataDTO transferDataDTO = new DassAssembleTransferDataDTO();
        transferDataDTO.setDassTransferDataDTO(getDassTransferData(transfer, shuHeContext));
        transferDataDTO.setPhoneSaleExtendInfo(getPhoneSaleExtendShuhe(transfer));
        return transferDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        boolean bool = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                    (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
            BaseUserType baseUserType = shuHeContext.getBaseUserType();
            if (baseUserType instanceof CuFuJie) {
                CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
                Date creatTime = shuHeContext.getCreatTime();
                Integer day = handlerService.getShuHePeriodOfValidityDay(caseShuheUser.getUserType());
                boolean isPeriod = baseUserType.dataPeriodOfValidity(iMarketingSyncUserService
                        , transfer.getCreateTime(), day, creatTime);
                if (isPeriod) {
                    bool = ((CuFuJie) baseUserType).ifTransfer(caseShuheUser, creatTime, marketingCommonConfig);
                }
            }
        }
        return bool;
    }

    @Override
    public String label() {
        return "ShuHe_TransferData_ArtificialTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.SHU_HE_RULE_DATA_COLLECTION.getCode();
    }

    private DassTransferDataDTO getDassTransferData(MarketingTransferSyncUser transfer, ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext) {
        DassTransferDataDTO dassTransferDataDTO = new DassTransferDataDTO();
        dassTransferDataDTO.setUid(transfer.getCustNum());
        dassTransferDataDTO.setSource("16");
        dassTransferDataDTO.setUserType("1");
        dassTransferDataDTO.setPhone(shuHeContext.getCaseShuheUser().getCell());
        dassTransferDataDTO.setType("4");
        dassTransferDataDTO.setOrgName("shuhefujie");
        dassTransferDataDTO.setTransformStatus("2");
        dassTransferDataDTO.setId(transfer.getId());
        return dassTransferDataDTO;
    }

    private PhoneSaleExtendInfo getPhoneSaleExtendShuhe(MarketingTransferSyncUser transfer) {
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        LocalDateTime localDateTime = transfer.getCreateTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDate localDate = localDateTime.toLocalDate();
        phoneSaleExtendInfo.setAppletDate(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        phoneSaleExtendInfo.setCustNum(transfer.getCustNum());
        phoneSaleExtendInfo.setAppletTime(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setDxType("4");
        phoneSaleExtendInfo.setSourceId(transfer.getId());
        return phoneSaleExtendInfo;
    }
}
