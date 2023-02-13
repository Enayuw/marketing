package com.br.marketing.service.Impl;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.util.PeriodOfValidityHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.function.Supplier;

/**
 * 实现具体有效期的计算
 *
 * @author Guo Zeqiang
 * @dateTime 2023-02-09 9:30
 */
@Service
@Slf4j
public class PeriodOfValidityServiceImpl implements IPeriodOfValidityService {

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;


    @Override
    public boolean isExpire(Date date, Integer day, Date validityDate) {
        return !isNotExpire(date, day, validityDate);
    }

    @Override
    public boolean isNotExpire(Date date, Integer day, Date validityDate) {
        if (ObjectUtils.isEmpty(validityDate)) {
            return false;
        }
        final LocalDate localDate = (date == null
                ? LocalDate.now() : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        final LocalDate localValidityDate = validityDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate firstDate;
        final LocalDate lastDate;
        if (day == null) {
            firstDate = localValidityDate;
            lastDate = localValidityDate.with(TemporalAdjusters.lastDayOfMonth());
        } else if (day > 0) {
            firstDate = localValidityDate;
            lastDate = localValidityDate.plusDays(day);
        } else if (day == 0) {
            firstDate = localValidityDate;
            lastDate = localValidityDate;
        } else {
            firstDate = localValidityDate.plusDays(day);
            lastDate = localValidityDate;
        }
        return (localDate.isAfter(firstDate) || localDate.isEqual(firstDate))
                && (localDate.isBefore(lastDate) || localDate.isEqual(lastDate));
    }


    @Override
    public boolean isExpire(Date date, String validityDayStr, Date validityDate) throws IllegalAccessException {
        return !isNotExpire(date, validityDayStr, validityDate);
    }

    @Override
    public boolean isNotExpire(Date date, String validityDayStr, Date validityDate) throws IllegalAccessException {
        Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(validityDayStr);
        return isNotExpire(date, day, validityDate);
    }

    @Override
    public boolean isExpire(Date date, Supplier<Object> validityDayStrSupplier, Supplier<Date> validityDateSupplier)
            throws IllegalAccessException {
        return !isNotExpire(date, validityDayStrSupplier, validityDateSupplier);
    }

    @Override
    public boolean isNotExpire(Date date, Supplier<Object> validityDayStrSupplier, Supplier<Date> validityDateSupplier)
            throws IllegalAccessException {
        final Object o = validityDayStrSupplier.get();
        if (o instanceof String) {
            return isNotExpire(date, (String) o, validityDateSupplier.get());
        } else if (o instanceof Integer) {
            return isNotExpire(date, (Integer) o, validityDateSupplier.get());
        } else {
            throw new IllegalAccessException("暂时只接受“String”或“Integer”数据类型的结果");
        }
    }

    @Override
    public boolean isExpire(String apiCode, String custNum, Date date, String validityDayStr)
            throws IllegalAccessException {
        return !isNotExpire(apiCode, custNum, date, validityDayStr);
    }

    @Override
    public boolean isNotExpire(String apiCode, String custNum, Date date, String validityDayStr)
            throws IllegalAccessException {
        MarketingSyncUser syncUser = new MarketingSyncUser();
        syncUser.setApiCode(apiCode);
        syncUser.setCustNum(custNum);
        Date validityDate = getAppletTimeBySyncUser(syncUser);
        Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(validityDayStr);
        return isNotExpire(date, day, validityDate);
    }

    @Override
    public boolean isExpire(String apiCode, String custNum, Date date, Integer day) {
        return !isNotExpire(apiCode, custNum, date, day);
    }

    @Override
    public boolean isNotExpire(String apiCode, String custNum, Date date, Integer day) {
        MarketingSyncUser syncUser = new MarketingSyncUser();
        syncUser.setApiCode(apiCode);
        syncUser.setCustNum(custNum);
        return isNotExpire(syncUser, date, day);
    }

    @Override
    public boolean isExpire(MarketingSyncUser syncUser, Date date, String validityDayStr)
            throws IllegalAccessException {
        return !isNotExpire(syncUser, date, validityDayStr);
    }

    @Override
    public boolean isNotExpire(MarketingSyncUser syncUser, Date date, String validityDayStr)
            throws IllegalAccessException {
        Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(validityDayStr);
        return isNotExpire(syncUser, date, day);
    }

    @Override
    public boolean isExpire(MarketingSyncUser syncUser, Date date, Integer day) {
        return !isNotExpire(syncUser, date, day);
    }

    @Override
    public boolean isNotExpire(MarketingSyncUser syncUser, Date date, Integer day) {
        Date validityDate = getAppletTimeBySyncUser(syncUser);
        return isNotExpire(date, day, validityDate);
    }

    @Override
    public PeriodOfValidityBO.Builder getPeriodOfValidityRange(String validityDayStr, Date validityDate)
            throws IllegalAccessException {
        Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(validityDayStr);
        return getPeriodOfValidityRange(day, validityDate);
    }

    @Override
    public PeriodOfValidityBO.Builder getPeriodOfValidityRange(Integer day, Date validityDate) {
        final ZonedDateTime creatDate = ObjectUtils.isEmpty(validityDate)
                ? ZonedDateTime.now() : validityDate.toInstant().atZone(ZoneId.systemDefault());
        final Instant firstInstant;
        final Instant lastInstant;
        if (day == null) {
            firstInstant = creatDate.toInstant();
            lastInstant = creatDate.with(TemporalAdjusters.lastDayOfMonth()).toInstant();
        } else if (day > 0) {
            firstInstant = creatDate.toInstant();
            lastInstant = creatDate.plusDays(day).toInstant();
        } else if (day == 0) {
            firstInstant = creatDate.toInstant();
            lastInstant = firstInstant;
        } else {
            firstInstant = creatDate.plusDays(day).toInstant();
            lastInstant = creatDate.toInstant();
        }
        return PeriodOfValidityBO.custom(Date.from(firstInstant), Date.from(lastInstant));
    }

    @Override
    public PeriodOfValidityBO.Builder getPeriodOfValidityRange(String apiCode, String custNum, String validityDayStr)
            throws IllegalAccessException {
        MarketingSyncUser syncUser = new MarketingSyncUser();
        syncUser.setApiCode(apiCode);
        syncUser.setCustNum(custNum);
        return getPeriodOfValidityRange(syncUser, validityDayStr);
    }

    @Override
    public PeriodOfValidityBO.Builder getPeriodOfValidityRange(String apiCode, String custNum, Integer day) {
        MarketingSyncUser syncUser = new MarketingSyncUser();
        syncUser.setApiCode(apiCode);
        syncUser.setCustNum(custNum);
        return getPeriodOfValidityRange(syncUser, day);
    }

    @Override
    public PeriodOfValidityBO.Builder getPeriodOfValidityRange(MarketingSyncUser syncUser, String validityDayStr)
            throws IllegalAccessException {
        Date validityDate = getAppletTimeBySyncUser(syncUser);
        return getPeriodOfValidityRange(validityDayStr, validityDate);
    }

    @Override
    public PeriodOfValidityBO.Builder getPeriodOfValidityRange(MarketingSyncUser syncUser, Integer day) {
        Date validityDate = getAppletTimeBySyncUser(syncUser);
        return getPeriodOfValidityRange(day, validityDate);
    }

    @Override
    public PeriodOfValidityBO.Builder getPeriodOfValidityRange(Supplier<Object> validityDayStrSupplier
            , Supplier<Date> validityDateSupplier) throws IllegalAccessException {
        final Object o = validityDayStrSupplier.get();
        if (o instanceof String) {
            return getPeriodOfValidityRange((String) o, validityDateSupplier.get());
        } else if (o instanceof Integer) {
            return getPeriodOfValidityRange((Integer) o, validityDateSupplier.get());
        } else {
            throw new IllegalAccessException("暂时只接受“String”或“Integer”数据类型的结果");
        }
    }

    private Date getAppletTimeBySyncUser(MarketingSyncUser syncUser) {
        MarketingSyncUser user = marketingSyncUserMapper.getAppletTimeBySyncUser(syncUser);
        return ObjectUtils.isEmpty(user) ? new Date() : (user.getAppletTime() == null
                ? (user.getCreateTime() == null
                ? new Date() : user.getCreateTime()) : user.getAppletTime());
    }
}
