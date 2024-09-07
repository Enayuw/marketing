package com.br.marketing.rule.shuhe;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.biocloo.input.BlackDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.api.client.util.Sets;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
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
public class ShuHeBlackListToBioclooImpl implements AssembleData<BlackDataDTO> {

    public final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public BlackDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.isEmpty(reserveField1)) {
            return null;
        }
        JSONObject json = JSON.parseObject(reserveField1);
        String isBlack = json.getString("is_black");
        DateTime nowDay = DateUtil.parse(LocalDate.now().toString(), DatePattern.NORM_DATE_PATTERN);
        String usrForbidCallEndTimStr = json.getString("usr_forbid_call_end_tim");
        String clcUsrMaxDxRrtEndStr = json.getString("clc_usr_max_dx_rrt_end");
        DateTime usrForbidCallEndTim = null;
        try {
            usrForbidCallEndTim = DateUtil.parse(usrForbidCallEndTimStr, DatePattern.NORM_DATE_PATTERN);
        } catch (Exception e) {
            log.warn("数禾促首借推送百可录黑名单,usrForbidCallEndTim日期格式转换失败,custNum:{}", transfer.getCustNum());
        }
        DateTime clcUsrMaxDxRrtEnd = null;
        try {
            clcUsrMaxDxRrtEnd = DateUtil.parse(clcUsrMaxDxRrtEndStr, DatePattern.NORM_DATE_PATTERN);
        } catch (Exception e) {
            log.warn("数禾促首借推送百可录黑名单,clcUsrMaxDxRrtEnd日期格式转换失败,custNum:{}", transfer.getCustNum());
        }
        boolean canPush = true;
        String expireDate = null;
        if (Objects.nonNull(usrForbidCallEndTim) && usrForbidCallEndTim.isAfterOrEquals(nowDay)) {
            expireDate = getExpireDate(json, "usr_forbid_call_end_tim");
        } else if (Objects.nonNull(clcUsrMaxDxRrtEnd) && clcUsrMaxDxRrtEnd.isAfterOrEquals(nowDay)) {
            expireDate = getExpireDate(json, "clc_usr_max_dx_rrt_end");
        } else if (Objects.equals("Y", isBlack)) {
            expireDate = getExpireDateForBlack();
        } else {
            canPush = false;
        }
        if (canPush) {
            BlackDataDTO blackDataDTO = new BlackDataDTO();
            JSONObject proxyJson = marketingCommonConfig.getShuHeProxyToBioclooApiCode();
            blackDataDTO.setApiCode(proxyJson.getString(context.getApiCode()));
            blackDataDTO.setCaseNum(transfer.getCustNum());
            String decode = BrCipherMaker.getInstance().decode(json.getString("cell"));
            blackDataDTO.setPhone(Md5Utils.cell32(decode));
            if (!StringUtils.isEmpty(expireDate)) {
                blackDataDTO.setExpireDate(expireDate);
            }
            log.warn("数禾促首借推送百可录黑名单,apiCode={},custNum={}", proxyJson.getString(context.getApiCode()), transfer.getCustNum());
            return blackDataDTO;
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
            JSONObject proxyJson = marketingCommonConfig.getShuHeProxyToBioclooApiCode();
            Set<String> custNumSet = Sets.newHashSet();
            custNumSet.add(transfer.getCustNum());
            List<MarketingSyncUser> syncUserList =
                marketingSyncUserMapper.getCellLastByCustNums(proxyJson.getString(context.getApiCode()), custNumSet);
            return !CollectionUtil.isEmpty(syncUserList);
        }
        return false;
    }

    @Override
    public String label() {
        return "ShuHe_BlackList_To_Biocloo";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.BIOCLOO_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

    private String getExpireDate(JSONObject json, String key) {
        Date date = json.getDate(key);
        if (date != null) {
            LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            String expireDate = localDateTime.format(DATE_FORMAT);
            if (expireDate.endsWith("00:00:00")) {
                expireDate = expireDate.substring(0, 10) + " 23:59:59";
            }
            return expireDate;
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
        expireDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).plusDays(blackDays).format(DATE_FORMAT);
        return expireDate;
    }
}
