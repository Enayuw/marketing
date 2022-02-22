package com.br.marketing.service.Impl;

import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.vo.TodayIdTimeBySoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

@Service
public class MarketingSyncUserImpl implements IMarketingSyncUserService {

    @Autowired
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public Long countRepeat(String execSql) {
        return marketingSyncInfoMapper.countRepeat(execSql);
    }

    @Override
    public TodayIdTimeBySoleVo getSoleValidUser(String execSql) {
        return marketingSyncInfoMapper.getSoleValidUser(execSql);
    }


    @Override
    public Integer updateRepeatUserStatus(String execSql) {
        return marketingSyncInfoMapper.updateRepeatUserStatus(execSql);
    }

    @Override
    public Boolean isPeriodOfValidity(String apiCode, String custNum, String userType, Date date, int day) {
        final LocalDateTime localDateTime = (date == null ? LocalDateTime.now()
                : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        final Date creatTime = getCreatTimeByCustNumAndUserType(apiCode, custNum, userType);
        if (ObjectUtils.isEmpty(creatTime)) {
            return false;
        }
        final LocalDateTime firstTime;
        final LocalDateTime lastTime;
        if (day > -1) {
            firstTime = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            if (day == 0) {
                lastTime = firstTime.with(TemporalAdjusters.lastDayOfMonth())
                        .withHour(23).withMinute(59).withSecond(59);
            } else {
                lastTime = firstTime.plusDays(day).withHour(23).withMinute(59).withSecond(59);
            }
        } else {
            lastTime = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            firstTime = lastTime.plusDays(day).withHour(0).withMinute(0).withSecond(0);
        }
        return (localDateTime.isAfter(firstTime) || localDateTime.isEqual(firstTime))
                && (localDateTime.isBefore(lastTime) || localDateTime.isEqual(lastTime));
    }

    @Override
    public String getUserTypeLatestByCustNum(String apiCode, String custNum) {
        return marketingSyncInfoMapper.getUserTypeLatestByCustNum(apiCode, custNum);
    }

    @Override
    public String getTaskIdLatestByCustNum(String apiCode, String custNum) {
        return marketingSyncInfoMapper.getTaskIdLatestByCustNum(apiCode, custNum);
    }

    @Override
    public String getAppletTimeByCustNumAndUserType(String apiCode, String custNum, String userType) {
        return marketingSyncInfoMapper.getAppletTimeByCustNumAndUserType(apiCode, custNum, userType);
    }

    @Override
    public Date getCreatTimeByCustNumAndUserType(String apiCode, String custNum, String userType) {
        return marketingSyncInfoMapper.getCreatTimeByCustNumAndUserType(apiCode, custNum, userType);
    }
}
