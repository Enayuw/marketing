package com.br.marketing.rule.shuhe;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeCuShouDengRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 数禾促首登推送客服转化过滤
 * @Author hong.chen
 * 技术方案地址：https://c.100credit.cn/pages/viewpage.action?pageId=166637179
 * @CreateTime 2024/06/25
 */
@Service
@Slf4j
public class ShuHeCuShouDengCustomerTransferImpl implements AssembleData<ConversionData> {
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ShuHeCuShouDengRuleCollectDataImpl.ShuHeCuShouDengRuleNecessaryData necessaryData =
                (ShuHeCuShouDengRuleCollectDataImpl.ShuHeCuShouDengRuleNecessaryData) context.getRuleNecessaryData();
        String custNum = transfer.getCustNum();

        Map<String, SyncUserValidityPeriodsBO> boMap = necessaryData.getPeriodBOMap();
        SyncUserValidityPeriodsBO bo = boMap.get(custNum);
        MarketingSyncUser syncUser = bo.getSyncUsers().get(0);

        PeriodOfValidityBO.Builder builder = bo.getBuilders().get(0);
        PeriodOfValidityBO periodOfValidityBO = builder.addDateString().addOfDayTimeStrString().builder();
        String enDateStr = periodOfValidityBO.getEndOfDayTimeStr();

        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);

        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(custNum);
        conversionData.setInversionStatus("0");
        conversionData.setExpireDate(enDateStr);
        conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        conversionData.setPhone(BrCipherMaker.getInstance().decode(syncUser.getCell()));
        conversionData.setInversionInfo(JSON.toJSONString(vo));

        log.warn("数禾促首登转化过滤,apicode={},custNum={}", syncUser.getApiCode(), custNum);
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;

            List<String> userTypeList = marketingCommonConfig.getShuHeCuShouDengTransferApiCodeMapping().get(context.getApiCode());
            if (CollectionUtils.isEmpty(userTypeList)) {
                return false;
            }

            if (!userTypeList.contains(transfer.getUserType())) {
                return false;
            }

            ShuHeCuShouDengRuleCollectDataImpl.ShuHeCuShouDengRuleNecessaryData necessaryData =
                    (ShuHeCuShouDengRuleCollectDataImpl.ShuHeCuShouDengRuleNecessaryData) context.getRuleNecessaryData();
            Map<String, SyncUserValidityPeriodsBO> boMap = necessaryData.getPeriodBOMap();
            String custNum = transfer.getCustNum();

            if (CollectionUtils.isEmpty(boMap)) {
                log.warn("数禾促首登转化过滤，该custNum不在有效期：{}", custNum);
                return false;
            }

            SyncUserValidityPeriodsBO periodsBO = boMap.get(custNum);
            if (periodsBO == null) {
                log.warn("数禾促首登转化过滤，该custNum不在有效期：{}", custNum);
                return false;
            }

            List<MarketingSyncUser> syncUsers = periodsBO.getSyncUsers();
            if (CollectionUtils.isEmpty(syncUsers)) {
                log.warn("数禾促首登转化过滤，该custNum不在有效期：{}", custNum);
                return false;
            }

            // clc_usr_lst_app_sta_tim（用户最新一次app渠道申完时间）> 该custNum有效期内的上传时间
            MarketingSyncUser syncUser = syncUsers.get(0);
            JSONObject json = JSON.parseObject(transfer.getReserveField1());
            Date clcUsrLstAppStaTim = json.getDate("clc_usr_lst_app_sta_tim");
            if (Objects.nonNull(clcUsrLstAppStaTim) && clcUsrLstAppStaTim.after(syncUser.getAppletTime())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String label() {
        return "ShuHe_CuShouDeng_TransferData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.SHUHE_CUSHOUDENG_RULE_DATA_COLLECTION.getCode();
    }
}
