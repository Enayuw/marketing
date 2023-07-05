package com.br.marketing.rule.xiecheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * @Description : 携程自动化过滤推送客服转化一对多分发(3710058推3710058、3710078)
 * http://c.100credit.cn/pages/viewpage.action?pageId=113828210
 * ---------------------------------
 * @Author : hong.chen
 * @Date : Create in 2023/07/05 10:28
 */
@Service
@Slf4j
public class XieChengCustomerTransferDistributeAImpl implements AssembleData<ConversionData> {

    public static final String ONE_TO_MANY = "oneToMany";
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        log.warn("携程推客服转化,apicode={}",transfer.getApiCode());
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setInversionStatus("0");
        String query = RpcClientProxy.decode(transfer.getCustNum(), "cell", "sha", "");
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
        boolean flag = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)) {
                JSONObject json = JSON.parseObject(reserveField1);
                Integer convType = json.getInteger("convType");
//                flag = !StringUtils.isEmpty(convType) && 106 == convType;

                List<Integer> convTypeList = marketingCommonConfig.getTransferConvTypeConfig().get(transfer.getApiCode()).get(ONE_TO_MANY);
                if (convTypeList.contains(convType)) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public String label() {
        return "XieCheng_TransferData_CustomerTransfer_distribute";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_DISTRIBUTE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
