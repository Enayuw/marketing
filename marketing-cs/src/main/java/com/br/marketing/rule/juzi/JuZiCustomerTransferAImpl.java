package com.br.marketing.rule.juzi;

import com.alibaba.fastjson.JSON;
import com.br.common.util.DateUtils;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 *
 * @Description : 桔子客服转化规则
 * ---------------------------------
 * @Author : juanjuan.song
 * @Date : Create in 2022/10/17 10:28
 */
@Service
@Slf4j
public class JuZiCustomerTransferAImpl implements AssembleData<ConversionData> {

    protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");

    @Autowired
    DecodeClient decodeClient;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        log.warn("桔子推送客服转化，apicode={}",transfer.getApiCode());
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setInversionStatus("0");
        conversionData.setCaseNum(transfer.getCustNum());
        try{
            if("0".equals(transfer.getApplyResult()) && !StringUtils.isEmpty(transfer.getApplyDt())){
                LocalDate parse = LocalDate.parse(transfer.getApplyDt(), dateTimeFormatter);
                LocalDate plusDays = parse.plusDays(30);
                conversionData.setExpireDate(plusDays + " 23:59:59");
            }
            if("0".equals(transfer.getUnlentAmount()) && !StringUtils.isEmpty(transfer.getLentTime())){
                LocalDate parse = LocalDate.parse(transfer.getLentTime(), dateTimeFormatter);
                LocalDate plusDays = parse.plusDays(30);
                conversionData.setExpireDate(plusDays + " 23:59:59");
            }
        }catch (DateTimeParseException e){
            log.warn("日期转换出错！applyDt或者lentTime不符合yyyy-MM-dd HH:mm:ss[:SSS]格式！");
        }
        String query = decodeClient.query(transfer.getCustNum(), "cell", "md5", "");
        conversionData.setPhone(query);
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
        boolean bool = "0".equals(transfer.getApplyResult()) || "0".equals(transfer.getUnlentAmount());
        log.warn("桔子推客服转化标识：{}",bool);
        return bool;
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
