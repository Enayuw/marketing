package com.br.marketing.rule.shuhe;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.biocloo.input.BlackDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.shuhe.util.ShuHeBlackListUtil;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.api.client.util.Sets;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * <a href="https://c.100credit.cn/pages/viewpage.action?pageId=178192891">【紧急】D20240906数禾促首借自动化转黑名单（营销→bkl）-3710166</a>
 *
 * @author senyang.zheng
 * @date 2024/09/07
 */
@Service
@Slf4j
public class ShuHeTransferToBioclooImpl implements AssembleData<BlackDataDTO.DataDTO> {

    public final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private TransferDataValidityPeriodService validityPeriodService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public BlackDataDTO.DataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        String reserveField1 = transfer.getReserveField1();
        JSONObject json = JSON.parseObject(reserveField1);
        String expireDate = ShuHeBlackListUtil.getBlackDataExpireDate(transfer, marketingCommonConfig.getShuhePushBlackDay());
        if (StringUtils.isNotEmpty(expireDate)) {
            BlackDataDTO.DataDTO dataDTO = new BlackDataDTO.DataDTO();
            JSONObject proxyJson = marketingCommonConfig.getShuHeProxyToBioclooApiCode();
            dataDTO.setApiCode(proxyJson.getString(context.getApiCode()));
            dataDTO.setCaseNum(transfer.getCustNum());
            String decode = BrCipherMaker.getInstance().decode(json.getString("cell"));
            dataDTO.setPhone(Md5Utils.cell32(decode));
            if (!StringUtils.isEmpty(expireDate)) {
                dataDTO.setExpireDate(expireDate);
            }
            log.warn("数禾促首借推送百可录黑名单,apiCode={},custNum={}", proxyJson.getString(context.getApiCode()), transfer.getCustNum());
            return dataDTO;
        }
        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {

        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
            String userType = transfer.getUserType();
            if (!"促首借".equals(userType)) {
                return false;
            }
            Set<String> custNumSet = Sets.newHashSet();
            String custNum = transfer.getCustNum();
            custNumSet.add(custNum);
            JSONObject proxyJson = marketingCommonConfig.getShuHeProxyToBioclooApiCode();
            Map<String, SyncUserValidityPeriodsBO> boMap =
                validityPeriodService.getValidityPeriodsByCustNum(custNumSet, proxyJson.getString(context.getApiCode()), new Date());
            if (CollectionUtils.isEmpty(boMap)) {
                log.warn("数禾促首借推送百可录黑名单，该custNum不在有效期：{}", custNum);
                return false;
            }
            SyncUserValidityPeriodsBO periodsBO = boMap.get(custNum);
            if (periodsBO == null) {
                log.warn("数禾促首借推送百可录黑名单，该custNum不在有效期：{}", custNum);
                return false;
            }
            List<MarketingSyncUser> syncUsers = periodsBO.getSyncUsers();
            if (CollectionUtils.isEmpty(syncUsers)) {
                log.warn("数禾促首借推送百可录黑名单，该custNum不在有效期：{}", custNum);
                return false;
            }
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.isNotEmpty(reserveField1) && JSON.isValid(reserveField1)) {
                JSONObject reserveFieldObject = JSONObject.parseObject(reserveField1);
                String usrLoanSucBtcashLimt1st = reserveFieldObject.getString("usr_loan_suc_btcash_limt_1st");
                return StringUtils.isNotEmpty(usrLoanSucBtcashLimt1st);
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "ShuHe_TransferData_To_Biocloo";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.BIOCLOO_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
