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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * D20230302桔子自动化过滤-3710075（营销→外呼）
 * http://c.100credit.cn/pages/viewpage.action?pageId=103553212
 *
 * @author Guo Zeqiang
 * @dateTime 2023/3/15 9:45
 */
@Service
@Slf4j
public class OrangeCustomerTransferImpl implements AssembleData<ConversionData> {

    protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setInversionStatus("0");
        conversionData.setCid("3874");
        conversionData.setCaseNum(transfer.getCustNum());
        // TODO: 2023-03-14 yyyy-mm-dd HH:mm:ss 添加有效期截至时间
        conversionData.setExpireDate("");
        String query = RpcClientProxy.decode(transfer.getCustNum(), "cell", "md5", "");
        conversionData.setPhone(query);
        if (StringUtils.isEmpty(transfer.getCreateTime())) {
            conversionData.setPartnerProcessDate(LocalDateTime.now().format(dateTimeFormatter));
        } else {
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String auditAmount = transfer.getAuditAmount();
            String lentAmount = transfer.getLentAmount();
            if (StringUtils.hasText(auditAmount) && StringUtils.hasText(lentAmount)) {
                BigDecimal a;
                BigDecimal l;
                try {
                    a = new BigDecimal(auditAmount);
                    l = new BigDecimal(lentAmount);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return false;
                }
                boolean bool = a.subtract(l).doubleValue() < 1000;
                // TODO: 2023-03-14  需要添加有效期判断结果
                if (bool) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "orange_TransferData_CustomerTransfer_v2";
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
