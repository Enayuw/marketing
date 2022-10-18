package com.br.marketing.rule.juzi;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @Description : 桔子客服转化规则
 * ---------------------------------
 * @Author : juanjuan.song
 * @Date : Create in 2022/10/17 10:28
 */
@Service
public class JuZiCustomerTransferAImpl implements AssembleData<ConversionData> {

    protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setInversionStatus("0");
        if(!StringUtils.isEmpty(transfer.getApplyDt()) && "0".equals(transfer.getApplyResult())){
            LocalDate parse = LocalDate.parse(transfer.getApplyDt(), dateTimeFormatter);
            LocalDate plusDays = parse.plusDays(30);
            conversionData.setExpireDate(plusDays.toString());
        }
        if(!StringUtils.isEmpty(transfer.getUnlentAmount()) && "0".equals(transfer.getUnlentAmount())){
            LocalDate parse = LocalDate.parse(transfer.getUnlentAmount(), dateTimeFormatter);
            LocalDate plusDays = parse.plusDays(30);
            conversionData.setExpireDate(plusDays.toString());
        }
        String md5 = transfer.getCustNum().substring(15);
        conversionData.setPhone(!StringUtils.isEmpty(md5) ? BrCipherMaker.getInstance().decode(md5) : "");
        if (!StringUtils.isEmpty(transfer.getCreateTime())){
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        /**
         * 转化数据上传接口命中applyResult=0的数据
         * 转化数据上传接口命中unlentAmount=0的数据
         */
        return "0".equals(transfer.getApplyResult()) || "0".equals(transfer.getUnlentAmount());
    }

    @Override
    public String label() {
        return "JuZi_TransferData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
