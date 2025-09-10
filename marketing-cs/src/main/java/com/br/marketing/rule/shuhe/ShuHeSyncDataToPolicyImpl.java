package com.br.marketing.rule.shuhe;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;

/**
 * D20231218数禾电销数据自动化转决策
 * https://c.100credit.cn/pages/viewpage.action?pageId=141601244
 * @author chenh
 */
@Service
@Slf4j
public class ShuHeSyncDataToPolicyImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    PushRuleService pushRuleService;

    private static final String[] MONTH_CN = {
            "", "一月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "十一月", "十二月"
    };

    private static final String[] DAY_CN = {
            "", "一日", "二日", "三日", "四日", "五日", "六日", "七日", "八日", "九日", "十日",
            "十一日", "十二日", "十三日", "十四日", "十五日", "十六日", "十七日", "十八日", "十九日", "二十日",
            "二十一日", "二十二日", "二十三日", "二十四日", "二十五日", "二十六日", "二十七日", "二十八日", "二十九日", "三十日", "三十一日"
    };


    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
        Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
        if (pushCellEncPolicy != null && pushCellEncPolicy.get(context.getApiCode()) != null) {
            encType = pushCellEncPolicy.get(context.getApiCode());
        }
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
        pushMarketingUserDetailByRuleDTO.setInitId(syncUser.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(syncUser.getCustNum());
        // 手机号log解密  md5加密
        String cell = pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getCell()));
        pushMarketingUserDetailByRuleDTO.setPhone(cell);

        String apiCode = syncUser.getApiCode();
        pushMarketingUserDetailByRuleDTO.setBatchNumber(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + apiCode);

        String strategyCode = marketingCommonConfig.getShuheToJueCeStrategy().get(apiCode);
        pushMarketingUserDetailByRuleDTO.setStrategyCode(strategyCode);

        JSONObject varDto = new JSONObject();
        JSONObject parseObject = JSON.parseObject(syncUser.getReserveField1());
        String groupTypeNew = parseObject.getOrDefault("groupTypeNew", "").toString();
        if (StringUtils.isNotBlank(groupTypeNew)) {
            varDto.put("groupType", groupTypeNew);
        }

        HashMap<String, String> dataCleanMappingMap = marketingCommonConfig.getDataCleanMappingMap();
        String value = dataCleanMappingMap.get(apiCode);
        if (value != null) {
            List<String> dataCleanValue = marketingCommonConfig.getDataCleanValue();
            String customNameType = dataCleanValue != null ? dataCleanValue.get(0) : "customNameType";
            varDto.put(customNameType, parseObject.getOrDefault(value, "").toString());
        }
        varDto.put("orderId", syncUser.getCustNum());
        varDto.put("appletDate",syncUser.getAppletDate());
        varDto.putAll(parseObject);
        String userType = syncUser.getUserType();
        String rtUsrHvyMaxAvaLmt = parseObject.getOrDefault("rt_usr_hvy_max_ava_lmt", "").toString();
        String currentAvailableLimitDp = parseObject.getOrDefault("clc_usr_light_current_available_limit_dp", "").toString();
        if (StringUtils.isNotBlank(userType) && "促复借".equals(userType) && StringUtils.isNotBlank(rtUsrHvyMaxAvaLmt)){
            String rtUsrHvyMaxAvaLmtDerived = getReportAmount(rtUsrHvyMaxAvaLmt);
            varDto.put("rt_usr_hvy_max_ava_lmt_derived", rtUsrHvyMaxAvaLmtDerived);
        }
        if (StringUtils.isNotBlank(userType) && "轻资产".equals(userType) && StringUtils.isNotBlank(currentAvailableLimitDp)){
            String clcDerived = getReportAmount(currentAvailableLimitDp);
            varDto.put("clc_usr_light_current_available_limit_dp_derived", clcDerived);
        }

        String clcUsrLstNonDcpTrsTim = parseObject.getOrDefault("clc_usr_lst_non_dcp_trs_tim", "").toString();
        if (StringUtils.isNotBlank(clcUsrLstNonDcpTrsTim)){
            String clcUsrLstNonDcpTrsTimDerived = formatToChineseMonthDay(clcUsrLstNonDcpTrsTim);
            varDto.put("clc_usr_lst_non_dcp_trs_tim_derived", clcUsrLstNonDcpTrsTimDerived);
        }

        pushMarketingUserDetailByRuleDTO.setVariables(varDto);

        log.warn("数禾上传数据推送决策,apicode={}", apiCode);
        return pushMarketingUserDetailByRuleDTO;
    }

    private String getReportAmount(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        
        try {
            BigDecimal amount = new BigDecimal(value);
            BigDecimal threshold = new BigDecimal("5000");
            if (amount.compareTo(threshold) >= 0) {
                // 金额大于等于5000时，只保留前两位，其他位数为0
                String amountStr = amount.setScale(0, BigDecimal.ROUND_DOWN).toString();
                if (amountStr.length() <= 2) {
                    return amountStr;
                }
                StringBuilder result = new StringBuilder(amountStr.substring(0, 2));
                for (int i = 0; i < amountStr.length() - 2; i++) {
                    result.append("0");
                }
                return result.toString();
            } else {
                // 金额小于5000时，只保留整数部分
                return amount.setScale(0, BigDecimal.ROUND_DOWN).toString();
            }
        } catch (NumberFormatException e) {
            log.warn("金额格式错误，value={}", value);
            return value;
        }
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingSyncUser) {
            return true;
        }

        return false;
    }

    @Override
    public String label() {
        return "ShuHe_SyncData_Policy";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.INIT_TO_POLICY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

    public static String formatToChineseMonthDay(String dateTime) {
        if (ObjectUtil.isEmpty(dateTime)) {
            return "";
        }
        try {
            LocalDate date = LocalDate.parse(dateTime.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return MONTH_CN[date.getMonthValue()] + DAY_CN[date.getDayOfMonth()];
        } catch (DateTimeParseException e) {
            return dateTime;
        }
    }
}
