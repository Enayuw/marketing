package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeRuleCollectDataImpl;
import com.br.marketing.dto.shuhe.strategy.BaseUserType;
import com.br.marketing.dto.shuhe.strategy.ChongShen;
import com.br.marketing.dto.shuhe.strategy.CuFuJie;
import com.br.marketing.entity.CaseShuheUser;
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

import javax.annotation.Resource;
import java.util.Map;

/**
 * 数禾推送转化至客服转化 业务
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/17 19:35
 */
@Service
@Slf4j
public class ShuHeCustomerTransferImpl implements AssembleData<ConversionData> {
    private final static String HAS_TRANS_FER = "1";
    private final static String NO_HAS_TRANSFER = "0";
    @Resource
    private ITransferSyncUserService iTransferSyncUserService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setInversionStatus(HAS_TRANS_FER.equals(transfer.getIfTransform())
                ? NO_HAS_TRANSFER : (NO_HAS_TRANSFER.equals(transfer.getIfTransform())
                ? HAS_TRANS_FER : transfer.getIfTransform()));
        conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeRuleNecessaryData =
                (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> map = shuHeRuleNecessaryData.getCustomerMap();
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
        BaseUserType baseUserType = shuHeRuleNecessaryData.getBaseUserType();
        //促复借新增推送字段
        if (baseUserType instanceof CuFuJie || baseUserType instanceof ChongShen) {
            if (baseUserType instanceof CuFuJie) {
                conversionData.setInversionDate(transfer.getTransformTime());
                conversionData.setEffectiveDate(transfer.getRequestTime());
            }
            //生效截止时间
            String plusDays;
            Map<String, SyncUserValidityPeriodsBO> boMap = shuHeRuleNecessaryData.getUserValidityPeriodsBOMap();
            plusDays = boMap.get(transfer.getCustNum()).getBuilders().get(0).addDateString().builder().getEnDateStr();
            conversionData.setExpireDate(plusDays + " 23:59:59");
        }
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws IllegalAccessException {
        boolean bool = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            Integer isDelay = context.getMqFact().getIsDelay();
            if (isDelay == null || isDelay != 1) {
                MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
                ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext =
                        (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
                if (shuHeContext.isContinueJudgeRule()) {
                    BaseUserType baseUserType = shuHeContext.getBaseUserType();
                    CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
                    /* 2024-04-12 13:50 需求：
                     * title：D20240408数禾促复借数据有效期变更-3710043
                     * url：https://c.100credit.cn/pages/viewpage.action?pageId=155694311
                     *
                     * 2024年7月15日 需求
                     * D20240703数禾全场景取值逻辑&有效期变更-337
                     * https://c.100credit.cn/pages/viewpage.action?pageId=166647068
                     */
                    Map<String, SyncUserValidityPeriodsBO> periodsMap = shuHeContext.getUserValidityPeriodsBOMap();
                    SyncUserValidityPeriodsBO userValidityPeriodsBO = periodsMap.get(transfer.getCustNum());
                    if (userValidityPeriodsBO == null) {
                        return false;
                    }
                    MarketingSyncUser marketingSyncUser = userValidityPeriodsBO.getSyncUsers().get(0);
                    MarketingTransferSyncUser transferSyncUser = new MarketingTransferSyncUser();
                    transferSyncUser.setId(transfer.getId());
                    transferSyncUser.settCid(transfer.gettCid());
                    shuHeContext.setTransfer(transfer);
                    if (baseUserType.isTurn(caseShuheUser) || baseUserType.isEmpty(caseShuheUser)) {
                        transferSyncUser.setIfTransform("2");
                        ((MarketingTransferSyncUser) transmitFact).setIfTransform("2");
                        iTransferSyncUserService.updateByPrimaryKeySelective(transferSyncUser);
                        shuHeContext.setContinueJudgeRule(false);
                        bool = Boolean.TRUE;
                    } else {
                        boolean cuFuJieBool = (baseUserType instanceof CuFuJie && ((CuFuJie) baseUserType).ifTransfer(
                                caseShuheUser, marketingSyncUser.getAppletTime(), marketingCommonConfig));
                        if (cuFuJieBool || baseUserType.ifTransfer(caseShuheUser, marketingSyncUser.getAppletTime())) {
                            // 转化
                            transferSyncUser.setIfTransform("1");
                            ((MarketingTransferSyncUser) transmitFact).setIfTransform("1");
                            iTransferSyncUserService.updateByPrimaryKeySelective(transferSyncUser);
                            shuHeContext.setContinueJudgeRule(false);
                            bool = Boolean.TRUE;
                        }
                    }
                    shuHeContext.setTransfer(null);
                }
            }
        }
        return bool;
    }

    @Override
    public String label() {
        return "ShuHe_2_TransferData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.SHU_HE_RULE_DATA_COLLECTION.getCode();
    }
}
