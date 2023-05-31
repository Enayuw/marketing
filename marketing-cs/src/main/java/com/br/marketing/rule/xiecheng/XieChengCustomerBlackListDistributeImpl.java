package com.br.marketing.rule.xiecheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @Description XieChengCustomerBlackListImpl
 * @Author hong.chen
 * @CreateTime 2023/05/18
 */
@Service
@Slf4j
public class XieChengCustomerBlackListDistributeImpl implements AssembleData<BlackDetailDTO> {

    @Override
    public BlackDetailDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser) transmitFact;
        log.warn("携程推客服黑名单,转化数据apicode={}", transferSyncUser.getApiCode());
        BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
        blackDetailDTO.setDataId(String.valueOf(transferSyncUser.getId()));
        String phone = RpcClientProxy.decode(transferSyncUser.getCustNum(), "cell", "sha", "");
        blackDetailDTO.setPhone(phone);

        return blackDetailDTO;
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
        return "XieCheng_TransferData_CustomerBlackList_distribute";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_BLACKLIST_DISTRIBUTE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
