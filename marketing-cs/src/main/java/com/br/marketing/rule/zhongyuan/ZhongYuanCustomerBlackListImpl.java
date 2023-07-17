package com.br.marketing.rule.zhongyuan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @Description 中原黑名单自动化过滤
 * @Author hong.chen
 * @CreateTime 2023/06/09
 */
@Service
@Slf4j
public class ZhongYuanCustomerBlackListImpl implements AssembleData<ConversionData> {

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser) transmitFact;
        log.warn("中原消金推客服转化,转化数据apicode={}", transferSyncUser.getApiCode());
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transferSyncUser.getId().toString());
        conversionData.setCid(transferSyncUser.getCid());
        conversionData.setCaseNum(transferSyncUser.getCustNum());
        conversionData.setInversionStatus("2");
        if (!StringUtils.isEmpty(transferSyncUser.getCreateTime())){
            conversionData.setPartnerProcessDate(DateUtils.format(transferSyncUser.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }

        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        boolean flag = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            //转化数据上传接口命中isBlack=1的数据
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)) {
                JSONObject json = JSON.parseObject(reserveField1);
                Integer isBlack = json.getInteger("isBlack");
                flag = isBlack != null && 1 == isBlack;
            }
        }
        return flag;
    }

    @Override
    public String label() {
        return "ZhongYuan_TransferData_CustomerTransfer";
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
