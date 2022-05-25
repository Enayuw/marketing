package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PhoneSaleExtendServiceImpl {

    @Autowired
    RedisChgService redisChgService;

    public static final DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    /**
     * 是否符合d的情况
     *
     * @param phoneSales
     * @param dataStatus
     * @return
     */
    public boolean haluoSaleJudge(List<PhoneSaleExtendInfo> phoneSales, String dataStatus, String taskId) {
        List<PhoneSaleExtendInfo> sales = phoneSales.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .sorted(Comparator.comparing(PhoneSaleExtendInfo::getAppletDate).reversed()
                        .thenComparing(PhoneSaleExtendInfo::getCreateTime).reversed()).collect(Collectors.toList());
        Integer taskTimeDays = 35;
        Integer abcTimeDays = 5;
        Integer dTimeDays = 4;
        Integer dTimes = 7;
        HashSet status = new HashSet();
        status.add("a");
        status.add("b");
        status.add("d");
        HashSet defaultGroupA = new HashSet();
        defaultGroupA.add("a");
        defaultGroupA.add("b");
        defaultGroupA.add("c");
        HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
        if (haluoTransferRule != null) {
            taskTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("taskIddate", "35"));
            abcTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("ABCdate", "5"));
            dTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("Ddate", "4"));
            dTimes = Integer.valueOf(haluoTransferRule.getOrDefault("dTimes", "7"));
            String statusStr = haluoTransferRule.getOrDefault("status", "a,b,d");
            status = new HashSet<>(Arrays.asList(statusStr.split(",")));
        }
        if (!status.contains(dataStatus)) {
            return false;
        }
        LocalDate nowDate = LocalDate.now();
        if (dataStatus.equals("d")) {
            PhoneSaleExtendInfo lastSale = null;
            Integer dnum = 0;
            for (PhoneSaleExtendInfo sale : sales) {
                if (sale.getStatus().equals("d")) {
                    if (dnum == 0) {
                        lastSale = sale;
                    }
                    dnum++;
                }
            }
            if (dnum >= dTimes) {
                return false;
            }
            LocalDate lastDate = LocalDate.parse(lastSale.getAppletDate(), ymd);
            long until = lastDate.until(nowDate, ChronoUnit.DAYS);
            if (until >= dTimeDays) {
                return true;
            }
        } else {
            PhoneSaleExtendInfo phoneSaleExtendInfo = sales.get(0);
            String appletDate = phoneSaleExtendInfo.getAppletDate();
            if (appletDate.equals(nowDate.format(ymd))) {
                return false;
            }
            Optional<PhoneSaleExtendInfo> firstGroupA = sales.stream().filter(t -> defaultGroupA.contains(t.getStatus())).findFirst();
            if (firstGroupA.isPresent()) {
                PhoneSaleExtendInfo info = firstGroupA.get();
                LocalDate lastDate = LocalDate.parse(info.getAppletDate(), ymd);
                long until = lastDate.until(nowDate, ChronoUnit.DAYS);
                if (until >= abcTimeDays) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public String getHaluoStatus(MarketingTransferSyncUser transferSyncUser, MarketingSyncUser syncUser) {
        HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
        HashSet status = new HashSet();
        status.add("a");
        status.add("b");
        status.add("d");
        if (haluoTransferRule != null) {
            String statusStr = haluoTransferRule.getOrDefault("status", "a,b,d");
            status = new HashSet<>(Arrays.asList(statusStr.split(",")));
        }
        JSONObject jb = JSON.parseObject(transferSyncUser.getReserveField1());
        boolean a = "1".equals(transferSyncUser.getIfLogin())
                && (jb != null && org.apache.commons.lang3.StringUtils.isNotBlank(jb.getString("applyInformation")) && "0".equals(jb.getString("applyInformation")))
                && !"1".equals(transferSyncUser.getIfApply());

        boolean b = "1".equals(transferSyncUser.getIfLogin())
                && (jb != null && org.apache.commons.lang3.StringUtils.isNotBlank(jb.getString("applyInformation")) && "1".equals(jb.getString("applyInformation")))
                && !"1".equals(transferSyncUser.getIfApply());

        boolean c = "1".equals(transferSyncUser.getIfLogin())
                && (jb != null && org.apache.commons.lang3.StringUtils.isNotBlank(jb.getString("applyInformation")) && "1".equals(jb.getString("applyInformation")))
                && "1".equals(transferSyncUser.getIfApply())
                && "0".equals(transferSyncUser.getApplyResult());
        Double unlentAmount = Double.valueOf(org.apache.commons.lang3.StringUtils.isNotBlank(transferSyncUser.getUnlentAmount()) ? transferSyncUser.getUnlentAmount() : "0");
        boolean d = unlentAmount > 0;
        String statusStr = "";
        if (status.contains("d") && d) {
            statusStr = "d";
        } else if (status.contains("a") && a) {
            statusStr = "a";
        } else if (status.contains("b") && b) {
            statusStr = "b";
        } else if (status.contains("c") && c) {
            statusStr = "c";
        }
        return statusStr;
    }

    public Result savePhoneExtend(PhoneSaleExtendInfo info){
        Boolean lock = Boolean.FALSE;
        while (!lock) {
            Result<Boolean> booleanResult = addHaluoLock(info.getApiCode(), info.getTaskId(), info.getCustNum(), info.getStatus());
            //不需要等待
            if (!ResultCode.SUCCESS.getValue().equals(booleanResult.getCode())) {
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            lock = booleanResult.getData();
            //如满足需要等待再次获取
            if (!lock) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        info
    }

    private Result<Boolean> addHaluoLock(String apiCode, String taskId, String custNum, String status) {
        String key = RedisKeyConstant.haluoPushDx.concat(":")
                .concat(apiCode).concat(":")
                .concat(taskId).concat(":")
                .concat(custNum);
        Long setnx = redisChgService.setnx(key, status, 3);
        //已经被其他数据抢占锁了
        if (setnx.equals(0L)) {

            //如果当前数据不是d就不推
            if (!status.equals("d")) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }

            String s = redisChgService.get(key);

            //分布式锁的数据状态如果是d则都不推
            if (s.equals("d")) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }

            //如果当前数据状态是d 并且锁里的数据不是d 需要等待500ms然后再次获取锁
            if (status.equals("d")) {
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }

    private void removeHaluoLock(String apiCode, String taskId, String custNum, String status) {
        String key = RedisKeyConstant.haluoPushDx.concat(":")
                .concat(apiCode).concat(":")
                .concat(taskId).concat(":")
                .concat(custNum);
        String s = redisChgService.get(key);
        if (status.equals(s)) {
            redisChgService.del(key);
        }
    }

}
