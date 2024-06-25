package com.br.marketing.rule.shuhe;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeCuShouDengRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 数禾促首登推送客服黑名单
 * @Author hong.chen
 * @CreateTime 2024/06/25
 */
@Service
@Slf4j
public class ShuHeCuShouDengCustomerBlackListImpl implements AssembleData<BlackDetailDTO> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter ymhdms = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public BlackDetailDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.isEmpty(reserveField1)) {
            return null;
        }

        JSONObject json = JSON.parseObject(reserveField1);
        String isBlack = json.getString("is_black");
        String decode = BrCipherMaker.getInstance().decode(json.getString("cell"));

        DateTime nowDay = DateUtil.parse(LocalDate.now().toString(), DatePattern.NORM_DATE_PATTERN);
        String usrForbidCallEndTimStr = json.getString("usr_forbid_call_end_tim");
        String clcUsrMaxDxRrtEndStr = json.getString("clc_usr_max_dx_rrt_end");
        DateTime usrForbidCallEndTim = DateUtil.parse(usrForbidCallEndTimStr, DatePattern.NORM_DATE_PATTERN);
        DateTime clcUsrMaxDxRrtEnd = DateUtil.parse(clcUsrMaxDxRrtEndStr, DatePattern.NORM_DATE_PATTERN);

        boolean canPush = true;
        String expireDate = null;
        if (usrForbidCallEndTim.isAfterOrEquals(nowDay)) {
            expireDate = getExpireDate(json, "usr_forbid_call_end_tim");
        } else if (clcUsrMaxDxRrtEnd.isAfterOrEquals(nowDay)) {
            expireDate = getExpireDate(json, "clc_usr_max_dx_rrt_end");
        } else if (Objects.equals("Y", isBlack)) {
            expireDate = getExpireDateForBlack();
        } else {
            canPush = false;
        }

        if (canPush) {
            BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
            blackDetailDTO.setDataId(String.valueOf(transfer.getId()));
            if (!StringUtils.isEmpty(expireDate)) {
                blackDetailDTO.setExpireDate(expireDate);
            }
            blackDetailDTO.setPhone(decode);
            return blackDetailDTO;
        }

        return null;
    }

    private String getExpireDateForBlack() {
        String expireDate;
        HashMap<String, Integer> shuhePushBlackDay = marketingCommonConfig.getShuhePushBlackDay();
        Integer blackDays = 30;
        if (shuhePushBlackDay != null) {
            blackDays = shuhePushBlackDay.getOrDefault("customerBlack", 30);
        }
        expireDate = LocalDateTime.now()
                .withHour(23).withMinute(59).withSecond(59)
                .plusDays(blackDays).format(ymhdms);
        return expireDate;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;

            List<String> userTypeList = marketingCommonConfig.getShuHeCuShouDengApiCodeMapping().get(context.getApiCode());
            if (userTypeList.contains(transfer.getUserType())) {
                ShuHeCuShouDengRuleCollectDataImpl.ShuHeCuShouDengRuleNecessaryData necessaryData =
                        (ShuHeCuShouDengRuleCollectDataImpl.ShuHeCuShouDengRuleNecessaryData) context.getRuleNecessaryData();
                Map<String, SyncUserValidityPeriodsBO> boMap = necessaryData.getPeriodBOMap();
                String custNum = transfer.getCustNum();

                if (CollectionUtils.isEmpty(boMap)) {
                    log.warn("数禾促首登推送客服黑名单，该custNum不在有效期：{}", custNum);
                    return false;
                }

                SyncUserValidityPeriodsBO periodsBO = boMap.get(custNum);
                if (periodsBO == null) {
                    log.warn("数禾促首登推送客服黑名单，该custNum不在有效期：{}", custNum);
                    return false;
                }

                List<MarketingSyncUser> syncUsers = periodsBO.getSyncUsers();
                if (CollectionUtils.isEmpty(syncUsers)) {
                    log.warn("数禾促首登推送客服黑名单，该custNum不在有效期：{}", custNum);
                    return false;
                }
            }
        }

        return false;
    }

    @Override
    public String label() {
        return "ShuHe_CuShouDeng_TransferData_CustomerBlackList";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.SHUHE_CUSHOUDENG_RULE_DATA_COLLECTION.getCode();
    }

    private String getExpireDate(JSONObject json, String key) {
        Date date = json.getDate(key);
        if (date != null) {
            String expireDate = DATE_FORMAT.format(date);
            if (expireDate.endsWith("00:00:00")) {
                expireDate = expireDate.substring(0, 10) + "23:59:59";
                return expireDate;
            }
        }

        return null;
    }
}
