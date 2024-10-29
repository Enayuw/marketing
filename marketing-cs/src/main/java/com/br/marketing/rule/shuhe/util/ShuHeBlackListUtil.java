package com.br.marketing.rule.shuhe.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingTransferSyncUser;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ShuHeBlackListUtil {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getBlackDataExpireDate(MarketingTransferSyncUser transmitFact, HashMap<String, Integer> shuhePushBlackDay) {
        String reserveField1 = transmitFact.getReserveField1();
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
            log.warn("数禾促首借推送百可录黑名单,usrForbidCallEndTim日期格式转换失败,custNum:{}", transmitFact.getCustNum());
        }
        DateTime clcUsrMaxDxRrtEnd = null;
        try {
            clcUsrMaxDxRrtEnd = DateUtil.parse(clcUsrMaxDxRrtEndStr, DatePattern.NORM_DATE_PATTERN);
        } catch (Exception e) {
            log.warn("数禾促首借推送百可录黑名单,clcUsrMaxDxRrtEnd日期格式转换失败,custNum:{}", transmitFact.getCustNum());
        }
        String expireDate = null;
        if (Objects.nonNull(usrForbidCallEndTim) && usrForbidCallEndTim.isAfterOrEquals(nowDay)) {
            expireDate = getExpireDate(json, "usr_forbid_call_end_tim");
        } else if (Objects.nonNull(clcUsrMaxDxRrtEnd) && clcUsrMaxDxRrtEnd.isAfterOrEquals(nowDay)) {
            expireDate = getExpireDate(json, "clc_usr_max_dx_rrt_end");
        } else if (Objects.equals("Y", isBlack)) {
            expireDate = getExpireDateForBlack(shuhePushBlackDay);
        }
        return expireDate;
    }

    private static String getExpireDate(JSONObject json, String key) {
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

    private static String getExpireDateForBlack(HashMap<String, Integer> shuhePushBlackDay) {
        String expireDate;
        Integer blackDays = 30;
        if (shuhePushBlackDay != null) {
            blackDays = shuhePushBlackDay.getOrDefault("customerBlack", 30);
        }
        expireDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).plusDays(blackDays).format(DATE_FORMAT);
        return expireDate;
    }
}
