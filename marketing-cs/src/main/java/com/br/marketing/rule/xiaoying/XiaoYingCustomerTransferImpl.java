package com.br.marketing.rule.xiaoying;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.XiaoYingRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.ITransferSyncUserService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 小赢推送转化至客服转化 业务
 *
 * @author Guo Zeqiang
 * @dateTime 2022/5/26 15:35
 */
@Service
@Slf4j
public class XiaoYingCustomerTransferImpl implements AssembleData<ConversionData> {
    private final static String STATE_0 = "0";
    private final static String STATE_1 = "1";
    @Resource
    private ITransferSyncUserService iTransferSyncUserService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setInversionStatus(transfer.getIfTransform());
        if (!ObjectUtils.isEmpty(transfer.getCreateTime())) {
            String format = DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
            conversionData.setPartnerProcessDate(format);
        }
        XiaoYingRuleCollectDataImpl.XiaoYingRuleNecessaryData ruleNecessaryData =
                (XiaoYingRuleCollectDataImpl.XiaoYingRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> map = ruleNecessaryData.getCustomerMap();
        if (map != null && map.containsKey(transfer.getCustNum())) {
            MarketingSyncUser marketingSyncUser = map.get(transfer.getCustNum());
            conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
            conversionData.setTaskId(marketingSyncUser.getCusBatch());
        } else {
            conversionData.setPhone("");
            conversionData.setTaskId("");
        }
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws IllegalAccessException {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            List<String> apiCodes = marketingCommonConfig.getXiaoYingTransferPushRobotApiCodes();
            if (apiCodes == null) {
                apiCodes = Arrays.asList("3710022", "7412100");
            }
            if (apiCodes.contains(transfer.getApiCode())) {
                String ifApply = transfer.getIfApply();
                boolean bool = STATE_1.equals(ifApply) || (STATE_0.equals(ifApply)
                        && STATE_1.equals(transfer.getIfLogin()));
                if (bool) {
                    MarketingTransferSyncUser transferSyncUser = new MarketingTransferSyncUser();
                    transferSyncUser.setId(transfer.getId());
                    transferSyncUser.settCid(transfer.gettCid());
                    transferSyncUser.setIfTransform(STATE_1);
                    transfer.setIfTransform(STATE_0);
                    iTransferSyncUserService.updateByPrimaryKeySelective(transferSyncUser);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "XiaoYing_TransferData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.XIAO_YING_RULE_DATA_COLLECTION.getCode();
    }
}
