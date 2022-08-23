package com.br.marketing.rule.ppd;

import com.br.marketing.client.dassservice.input.transfer.DassAssembleTransferDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description : 拍拍贷电销转化接口
 * ---------------------------------
 * @Author : lizhen
 * @Date : Create in 2022/4/21 10:19
 */
@Service
public class PPDArtificialTransferImpl implements AssembleData<DassAssembleTransferDataDTO> {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public DassAssembleTransferDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        DassAssembleTransferDataDTO transferDataDTO = new DassAssembleTransferDataDTO();
        DassTransferDataDTO dassTransferDataDTO = new DassTransferDataDTO();
        dassTransferDataDTO.setApiCode(context.getApiCode());
        dassTransferDataDTO.setIfTransform("1");
        dassTransferDataDTO.setUid(transfer.getCustNum());
        dassTransferDataDTO.setOrgName("ppdai");
        dassTransferDataDTO.setId(transfer.getId());
        transferDataDTO.setDassTransferDataDTO(dassTransferDataDTO);
        transferDataDTO.setPhoneSaleExtendInfo(getPhoneSaleExtendPPD(transfer));
        return transferDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        Set<String> includeRules = context.getMqFact().getIncludeRules();
        if (!CollectionUtils.isEmpty(includeRules) && includeRules.contains(label())) {
            HashMap<String, List<String>> ppdCustomerType = marketingCommonConfig.getPpdCustomerType();
            boolean a = ppdCustomerType.get("lend").contains(context.getApiCode()) && "Y".equals(transfer.getIfLent());
            boolean b = ppdCustomerType.get("transform").contains(context.getApiCode())
                    && Arrays.asList("-1", "1").contains(transfer.getIfTransform());
            return a || b;
        }
        return false;
    }

    @Override
    public String label() {
        return "PPD_TransferData_ArtificialTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

    /**
     * 封装电销扩展数据
     */
    private PhoneSaleExtendInfo getPhoneSaleExtendPPD(MarketingTransferSyncUser transfer) {
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
        phoneSaleExtendInfo.setSourceId(transfer.getId());
        return phoneSaleExtendInfo;
    }
}
