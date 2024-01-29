package com.br.marketing.rule.juzi;

import com.alibaba.fastjson.JSON;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
 * 客服转化接口案件编号和手机号二选一必填，不满足则接收转化数据失败
 */
@Service
@Slf4j
public class JuZiCustomerTransferAImpl implements AssembleData<ConversionData> {

    protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setInversionStatus("0");
        try{
            if("0".equals(transfer.getApplyResult()) && !StringUtils.isEmpty(transfer.getApplyDt())){
                LocalDate parse = LocalDate.parse(transfer.getApplyDt(), dateTimeFormatter);
                LocalDate plusDays = parse.plusDays(30);
                conversionData.setExpireDate(plusDays + " 23:59:59");
            }
            if(("0".equals(transfer.getUnlentAmount()) || "0.00".equals(transfer.getUnlentAmount()))
                    && !StringUtils.isEmpty(transfer.getLentTime())){
                LocalDate parse = LocalDate.parse(transfer.getLentTime(), dateTimeFormatter);
                LocalDate plusDays = parse.plusDays(30);
                conversionData.setExpireDate(plusDays + " 23:59:59");
            }
        }catch (DateTimeParseException e){
            log.warn("日期转换出错！applyDt或者lentTime不符合yyyy-MM-dd HH:mm:ss[:SSS]格式！");
        }
        String query = RpcClientProxy.decode(transfer.getCustNum(), "cell", "md5", "");
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
        boolean bool1 = "0".equals(transfer.getApplyResult()) && !StringUtils.isEmpty(transfer.getApplyDt());
        boolean bool2 = ("0".equals(transfer.getUnlentAmount()) || "0.00".equals(transfer.getUnlentAmount())) && !StringUtils.isEmpty(transfer.getLentTime());
        log.warn("桔子推客服转化标识：{}",bool1 || bool2);
        return bool1 || bool2;
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
