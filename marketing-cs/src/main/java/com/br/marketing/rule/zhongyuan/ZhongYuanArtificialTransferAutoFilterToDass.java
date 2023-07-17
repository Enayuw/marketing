package com.br.marketing.rule.zhongyuan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.transfer.DassAssembleTransferDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @Description : 中原消金电销自动化过滤
 * ---------------------------------
 * @Author : zgc
 * @Date : Create in 2023/6/08 17:19
 */
@Service
public class ZhongYuanArtificialTransferAutoFilterToDass implements AssembleData<DassAssembleTransferDataDTO> {

    @Override
    public DassAssembleTransferDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        DassAssembleTransferDataDTO transferDataDTO = new DassAssembleTransferDataDTO();
        DassTransferDataDTO dassTransferDataDTO = new DassTransferDataDTO();
        dassTransferDataDTO.setApiCode(context.getApiCode());
        dassTransferDataDTO.setUid(transfer.getCustNum());
        dassTransferDataDTO.setOrgName("zhongyuanxj");
        dassTransferDataDTO.setId(transfer.getId());
        dassTransferDataDTO.setSource("30");
        dassTransferDataDTO.setTransformStatus("1");
        transferDataDTO.setDassTransferDataDTO(dassTransferDataDTO);
        transferDataDTO.setPhoneSaleExtendInfo(getPhoneSaleExtendZhongYuan(transfer));
        return transferDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            //转化数据上传接口命中isBlack=1的数据
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)) {
                JSONObject json = JSON.parseObject(reserveField1);
                Integer isBlack = json.getInteger("isBlack");
                return isBlack != null && 1 == isBlack;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public String label() {
        return "ZhongYuan_Artificial_TransferAutoFilter";
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
    private PhoneSaleExtendInfo getPhoneSaleExtendZhongYuan(MarketingTransferSyncUser transfer) {
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
