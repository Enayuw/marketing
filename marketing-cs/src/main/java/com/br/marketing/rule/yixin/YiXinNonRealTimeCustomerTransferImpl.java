package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
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

import java.util.Arrays;
import java.util.List;

/**
 * 非实时转化数据推送客服
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/29 14:45
 */
@Service
public class YiXinNonRealTimeCustomerTransferImpl implements AssembleData<ConversionData> {

    private final List<String> TYPE_LIST = Arrays.asList("4", "5", "6", "7", "8", "11", "12", "14", "15", "17", "18", "19");
    private final List<String> CID_LIST = Arrays.asList("14583", "772", "793", "773");
    private final List<String> API_CODE_LIST = Arrays.asList("3710012", "7412003", "7410787", "7492629", "7492630");

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setTransformType("2");
        conversionData.setInversionStatus("0");
        String datetimePattern = "yyyy-MM-dd HH:mm:ss";
        conversionData.setPartnerProcessDate(StringUtils.isEmpty(transfer.getCreateTime())
                ? null : DateUtils.format(transfer.getCreateTime(), datetimePattern));
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        return CID_LIST.contains(transfer.getCid())
                && API_CODE_LIST.contains(transfer.getApiCode())
                && TYPE_LIST.contains(transfer.getType());
    }

    @Override
    public String label() {
        return "YiXin_NonRealTime_CustomerTransfer";
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
