package com.br.marketing.rule.ppd;

import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 拍拍贷客户转化接口
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/4/20 17:19
 */
@Service
public class PPDCustomerTransferImpl implements AssembleData<ConversionData> {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setInversionStatus("0");
        if (!StringUtils.isEmpty(transfer.getCreateTime())){
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        HashMap<String, List<String>> ppdCustomerType = marketingCommonConfig.getPpdCustomerType();
        boolean a = ppdCustomerType.get("lend").contains(context.getApiCode()) && "Y".equals(transfer.getIfLent());
        boolean b = ppdCustomerType.get("transform").contains(context.getApiCode())
                && Arrays.asList("-1","1").contains(transfer.getIfTransform());
        return a || b;
    }

    @Override
    public String label() {
        return "PPD_TransferData_CustomerTransfer";
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
