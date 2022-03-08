package com.br.marketing.service.Impl;

import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.vo.TodayIdTimeBySoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        final Date creatTime = getCreatTimeByCustNumAndUserType(apiCode, custNum, userType);
        return isPeriodOfValidity(apiCode, custNum, userType, date, day, creatTime);
    }

    @Override
    public Boolean isPeriodOfValidity(String apiCode, String custNum, String userType, Date date, int day
            , Date validityDate) {
        final LocalDate localDate = (date == null ? LocalDate.now()
                : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        if (ObjectUtils.isEmpty(validityDate)) {
            return false;
        }
        LocalDate creatDate = validityDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate firstDate;
        final LocalDate lastDate;
        if (day > -1) {
            firstDate = creatDate;
            if (day == 0) {
                lastDate = creatDate.with(TemporalAdjusters.lastDayOfMonth());
            } else {
                lastDate = creatDate.plusDays(day);
            }
        } else {
            lastDate = creatDate;
            firstDate = creatDate.plusDays(day);
        }
        return (localDate.isAfter(firstDate) || localDate.isEqual(firstDate))
                && (localDate.isBefore(lastDate) || localDate.isEqual(lastDate));
    }

    @Override
    public Boolean isPeriodOfValidity(Date date, int day, Date validityDate) {
        final LocalDate localDate = (date == null ? LocalDate.now()
                : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        if (ObjectUtils.isEmpty(validityDate)) {
            return false;
        }
        LocalDate creatDate = validityDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate firstDate;
        final LocalDate lastDate;
        if (day > -1) {
            firstDate = creatDate;
            if (day == 0) {
                lastDate = creatDate.with(TemporalAdjusters.lastDayOfMonth());
            } else {
                lastDate = creatDate.plusDays(day);
            }
        } else {
            lastDate = creatDate;
            firstDate = creatDate.plusDays(day);
        }
        return (localDate.isAfter(firstDate) || localDate.isEqual(firstDate))
                && (localDate.isBefore(lastDate) || localDate.isEqual(lastDate));
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

    @Override
    public List<Map<String, Object>> getCreatTimeByCustNumAndUserTypeList(String apiCode, List<String> custNums
            , String userType) {
        return marketingSyncInfoMapper.getCreatTimeByCustNumAndUserTypeList(apiCode, custNums, userType);
    }
}
