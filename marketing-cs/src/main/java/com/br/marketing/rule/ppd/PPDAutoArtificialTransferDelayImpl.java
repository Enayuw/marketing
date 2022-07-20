package com.br.marketing.rule.ppd;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IScoreResultService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

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
 * @Description : 拍拍贷自动化转人工静置-3710014
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/6/15 17:12
 */

@Service
public class PPDAutoArtificialTransferDelayImpl implements AssembleData<MqFact> {

    @Autowired
    IScoreResultService iScoreResultService;

    @Override
    public MqFact assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(transfer.getId());
        return mqFact;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        // ifTransform=0 userType=1,2,3 转化数据静置
        if ("3".equals(transfer.getUserType())) {
            return "0".equals(transfer.getIfTransform());
        }
        boolean userType = "0".equals(transfer.getIfTransform()) && Arrays.asList("1", "2").contains(transfer.getUserType());
        if(userType){
            Result<String> conditionRes = iScoreResultService.isFilterScoreByTransfer(context.getApiCode(), this.label());
            if(!ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())){
                return userType;
            }
            Result<String> stringResult = iScoreResultService.filterScoreResByTransfer(context.getApiCode(), transfer.getCustNum(), conditionRes.getData());
            return ResultCode.SUCCESS.getValue().equals(stringResult.getCode());
        }
        return userType;
    }

    @Override
    public String label() {
        return "PPD_TransferData_ArtificialBatch_Delay";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.BATCH_MESSAGE_DELAY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
