package com.br.marketing.service.Impl;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.TodayIdTimeBySoleVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MarketingSyncUserImpl implements IMarketingSyncUserService {

    @Autowired
    MarketingSyncInfoMapper marketingSyncInfoMapper;
    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

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
    public Boolean isPeriodOfValidity(String apiCode, String custNum, String userType, Date date, Integer day) {
        final Date creatTime = getCreatTimeByCustNumAndUserType(apiCode, custNum, userType);
        return isPeriodOfValidity(date, day, creatTime);
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
    public Boolean isPeriodOfValidity(Date date, Integer day, Date validityDate) {
        final LocalDate localDate = (date == null ? LocalDate.now()
                : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        if (ObjectUtils.isEmpty(validityDate)) {
            return false;
        }
        LocalDate creatDate = validityDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate firstDate;
        final LocalDate lastDate;
        if (day == null) {
            firstDate = creatDate;
            lastDate = creatDate.with(TemporalAdjusters.lastDayOfMonth());
        } else if (day == 0) {
            firstDate = creatDate;
            lastDate = creatDate;
        } else if (day > 0) {
            firstDate = creatDate;
            lastDate = creatDate.plusDays(day);
        } else {
            lastDate = creatDate;
            firstDate = creatDate.plusDays(day);
        }
        return (localDate.isAfter(firstDate) || localDate.isEqual(firstDate))
                && (localDate.isBefore(lastDate) || localDate.isEqual(lastDate));
    }

    @Override
    public PeriodOfValidityBO.Builder getPeriodOfValidityRange(Integer day, Date validityDate) {
        if (ObjectUtils.isEmpty(validityDate)) {
            return null;
        }
        ZonedDateTime creatDate = validityDate.toInstant().atZone(ZoneId.systemDefault());
        Instant firstInstant;
        Instant lastInstant;
        if (day == null) {
            firstInstant = creatDate.toInstant();
            lastInstant = creatDate.with(TemporalAdjusters.lastDayOfMonth()).toInstant();
        } else if (day == 0) {
            firstInstant = creatDate.toInstant();
            lastInstant = firstInstant;
        } else if (day > 0) {
            firstInstant = creatDate.toInstant();
            lastInstant = creatDate.plusDays(day).toInstant();
        } else {
            lastInstant = creatDate.toInstant();
            firstInstant = creatDate.plusDays(day).toInstant();
        }
        return PeriodOfValidityBO.custom(Date.from(firstInstant), Date.from(lastInstant));
    }

    @Override
    public String getUserTypeLatestByCustNum(String apiCode, String custNum) {
        return marketingSyncInfoMapper.getUserTypeLatestByCustNum(apiCode, custNum);
    }

    @Override
    public String getTaskIdLatestByCustNum(String apiCode, String custNum, String userType) {
        return marketingSyncInfoMapper.getTaskIdLatestByCustNum(apiCode, custNum, userType);
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
    public Map<String, Date> getSyncUserTimeMaxByCustNumsMap(String apiCode, Set<String> custNums, String userType
            , String dateTimeEnd) {
        List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserTimeMaxByCustNums(
                apiCode, custNums, userType, dateTimeEnd);
        return syncUserList.parallelStream().collect(Collectors.toMap(
                MarketingSyncUser::getCustNum, MarketingSyncUser::getCreateTime, (k1, k2) -> k2));
    }

    @Override
    public List<MarketingSyncUser> getFreeUserTypeAndDateAllFieldList(String apiCode, Set<String> custNumSet
            , Map<String, Set<String>> freeUserTypeAndDateMap) {
        return marketingSyncUserMapper.getFreeUserTypeAndDateAllFieldList(apiCode, custNumSet, freeUserTypeAndDateMap);
    }

    @Override
    public Map<String, List<MarketingSyncUser>> getFreeUserTypeAndDateAllFieldMap(String apiCode
            , Set<String> custNumSet, Map<String, Set<String>> freeUserTypeAndDateMap) {
        List<MarketingSyncUser> list = getFreeUserTypeAndDateAllFieldList(apiCode, custNumSet, freeUserTypeAndDateMap);
        return getGroupByCustNumMap(list);
    }

    @Override
    public List<MarketingSyncUser> getFreeUserTypeAndDateList(String apiCode, Set<String> custNumSet
            , Map<String, Set<String>> freeUserTypeAndDateMap) {
        return marketingSyncUserMapper.getFreeUserTypeAndDateList(apiCode, custNumSet, freeUserTypeAndDateMap);
    }

    @Override
    public Map<String, List<MarketingSyncUser>> getFreeUserTypeAndDateMap(String apiCode, Set<String> custNumSet, Map<String, Set<String>> freeUserTypeAndDateMap) {
        List<MarketingSyncUser> list = getFreeUserTypeAndDateList(apiCode, custNumSet, freeUserTypeAndDateMap);
        return getGroupByCustNumMap(list);
    }

    @Override
    public Map<String, MarketingSyncUser> getFreeUserTypeAndDateAllFieldMapValueOne(String apiCode
            , Set<String> custNumSet, Map<String, Set<String>> freeUserTypeAndDateMap) {
        List<MarketingSyncUser> list = getFreeUserTypeAndDateAllFieldList(apiCode, custNumSet, freeUserTypeAndDateMap);
        return getGroupByCustNumMapValueOne(list);
    }

    @Override
    public Map<String, MarketingSyncUser> getFreeUserTypeAndDateMapValueOne(String apiCode, Set<String> custNumSet
            , Map<String, Set<String>> freeUserTypeAndDateMap) {
        List<MarketingSyncUser> list = getFreeUserTypeAndDateList(apiCode, custNumSet, freeUserTypeAndDateMap);
        return getGroupByCustNumMapValueOne(list);
    }

    @Override
    public Map<String, MarketingSyncUser> getFreeUserTypeAndDateMapValueOne(String apiCode, Set<String> custNumSet) {
        return getFreeUserTypeAndDateMapValueOne(apiCode, custNumSet, marketingCommonConfig.getFreeUserTypeAndDateMap());
    }

    /**
     * 2022/9/22 17:40
     * 根据CustNum分组并且获取AppletDate与CreateTime取最新的一条记录
     */
    private Map<String, MarketingSyncUser> getGroupByCustNumMapValueOne(List<MarketingSyncUser> list) {
        return CollectionUtils.isEmpty(list) ? null :
                list.parallelStream().collect(Collectors.toMap(MarketingSyncUser::getCustNum, Function.identity()
                        , (v1, v2) -> (StringUtils.isNotBlank(v1.getAppletDate())
                                && StringUtils.isNotBlank(v2.getAppletDate())
                                && LocalDate.parse(v1.getAppletDate(), DateTimeFormatter.ISO_LOCAL_DATE)
                                .isBefore(LocalDate.parse(v2.getAppletDate(), DateTimeFormatter.ISO_LOCAL_DATE)))
                                && (!ObjectUtils.isEmpty(v1.getCreateTime())
                                && !ObjectUtils.isEmpty(v2.getCreateTime())
                                && v1.getCreateTime().before(v2.getCreateTime())) ? v2 : v1));
    }

    /**
     * 2022/9/22 17:40
     * 根据 CustNum分组
     */
    private Map<String, List<MarketingSyncUser>> getGroupByCustNumMap(List<MarketingSyncUser> list) {
        return CollectionUtils.isEmpty(list) ? null : list.stream().collect(
                Collectors.groupingBy(MarketingSyncUser::getCustNum));
    }

    @Override
    public Map<String, MarketingSyncUser> getCellByCellAndMaxAppletTimeMap(String apiCode
            , Set<String> cellSet) {
        List<MarketingSyncUser> cellByCellAndMaxAppletTime = marketingSyncUserMapper.getCellByCellAndMaxAppletTime(
                apiCode, cellSet);
        if (CollectionUtils.isEmpty(cellByCellAndMaxAppletTime)) {
            return null;
        }
        return cellByCellAndMaxAppletTime.parallelStream().collect(Collectors.toConcurrentMap(MarketingSyncUser::getCell
                , Function.identity()));
    }

    @Override
    public Boolean existUploadTable(String apiCode) {
        Integer res = marketingSyncUserMapper.existUploadTable("b_marketing_sync_".concat(apiCode));
        return (res != null && res >= 1) ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public List<MarketingSyncUser> noDesUploadByMinId(String apiCode,Long minId) {
        List<MarketingSyncUser> marketingSyncUsers = marketingSyncUserMapper.noDesUploadByMinIdtikv_(apiCode, minId);
        return marketingSyncUsers;
    }

    @Override
    public Long noDesUploadOfMinId(String apiCode) {
        return marketingSyncUserMapper.noDesUploadByMinIdtiflash_(apiCode);
    }

    @Override
    public Integer updateSqlByNoDes(String updateSql) {
        return marketingSyncUserMapper.updateSqlByNoDestikv_(updateSql);
    }
}
