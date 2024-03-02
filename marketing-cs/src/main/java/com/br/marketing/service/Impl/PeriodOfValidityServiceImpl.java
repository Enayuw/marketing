package com.br.marketing.service.Impl;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomizeDataValidConfigMapper;
import com.br.marketing.mapper.MarketingDataValidConfigDefaultMapper;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.util.PeriodOfValidityHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    @Resource
    private MarketingDataValidConfigDefaultMapper marketingDataValidConfigDefaultMapper;

    @Resource
    private MarketingCustomizeDataValidConfigMapper marketingCustomizeDataValidConfigMapper;


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
    public boolean isExpire(Date date, String validityDayStr, Date validityDate) throws IllegalArgumentException {
        return !isNotExpire(date, validityDayStr, validityDate);
    }

    @Override
    public boolean isNotExpire(Date date, String validityDayStr, Date validityDate) throws IllegalArgumentException {
        if (StringUtils.isBlank(validityDayStr)) {
            return false;
        }
        Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(validityDayStr, validityDate);
        return isNotExpire(date, day, validityDate);
    }

    @Override
    public boolean isExpire(Date date, Supplier<Object> validityDayStrSupplier, Supplier<Date> validityDateSupplier)
            throws IllegalArgumentException {
        return !isNotExpire(date, validityDayStrSupplier, validityDateSupplier);
    }

    @Override
    public boolean isNotExpire(Date date, Supplier<Object> validityDayStrSupplier, Supplier<Date> validityDateSupplier)
            throws IllegalArgumentException {
        final Object o = validityDayStrSupplier.get();
        if (o instanceof String) {
            return isNotExpire(date, (String) o, validityDateSupplier.get());
        } else if (o instanceof Integer) {
            return isNotExpire(date, (Integer) o, validityDateSupplier.get());
        } else {
            throw new IllegalArgumentException("暂时只接受“String”或“Integer”数据类型的结果");
        }
    }

    @Override
    public boolean isExpire(String apiCode, String custNum, Date date, String validityDayStr)
            throws IllegalArgumentException {
        return !isNotExpire(apiCode, custNum, date, validityDayStr);
    }

    @Override
    public boolean isNotExpire(String apiCode, String custNum, Date date, String validityDayStr)
            throws IllegalArgumentException {
        if (StringUtils.isBlank(validityDayStr)) {
            return false;
        }
        MarketingSyncUser syncUser = new MarketingSyncUser();
        syncUser.setApiCode(apiCode);
        syncUser.setCustNum(custNum);
        Date validityDate = getAppletTimeBySyncUser(syncUser);
        Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(validityDayStr, validityDate);
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
    public List<String> isExpire(String apiCode, Set<String> custNumSet, Date date, String validityDayStr)
            throws IllegalArgumentException {
        List<String> custNums = new ArrayList<>();
        List<MarketingSyncUser> list = marketingSyncUserMapper.getSyncUserLastByCustNums(apiCode
                , new ArrayList<>(custNumSet));
        for (MarketingSyncUser syncUser : list) {
            if (isExpire(date, validityDayStr
                    , (syncUser.getAppletTime() == null ? syncUser.getCreateTime() : syncUser.getAppletTime()))) {
                custNums.add(syncUser.getCustNum());
            }
        }
        return custNums;
    }

    @Override
    public List<String> isNotExpire(String apiCode, Set<String> custNumSet, Date date, String validityDayStr)
            throws IllegalArgumentException {
        List<String> custNums = new ArrayList<>();
        List<MarketingSyncUser> list = marketingSyncUserMapper.getSyncUserLastByCustNums(apiCode
                , new ArrayList<>(custNumSet));
        for (MarketingSyncUser syncUser : list) {
            if (isNotExpire(date, validityDayStr
                    , (syncUser.getAppletTime() == null ? syncUser.getCreateTime() : syncUser.getAppletTime()))) {
                custNums.add(syncUser.getCustNum());
            }
        }
        return custNums;
    }

    @Override
    public boolean isExpire(String dataDateStr, String validityDayStr, DateTimeFormatter dtf) {
        if (StringUtils.isBlank(dataDateStr)) {
            throw new NullPointerException("dataDateStr为NULL");
        }
        if (dtf == null) {
            dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }
        LocalDate dataDate = LocalDate.parse(dataDateStr, dtf);
        Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(validityDayStr);
        LocalDate startDate = LocalDate.now().minusDays(day);
        return dataDate.compareTo(startDate) < 0;
    }

    @Override
    public boolean isExpire(MarketingSyncUser syncUser, Date date, String validityDayStr)
            throws IllegalArgumentException {
        return !isNotExpire(syncUser, date, validityDayStr);
    }

    @Override
    public boolean isNotExpire(MarketingSyncUser syncUser, Date date, String validityDayStr)
            throws IllegalArgumentException {
        if (StringUtils.isBlank(validityDayStr)) {
            return false;
        }
        Date validityDate = getAppletTimeBySyncUser(syncUser);
        Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(validityDayStr, validityDate);
        return isNotExpire(date, day, validityDate);
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
            throws IllegalArgumentException {
        Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(validityDayStr, validityDate);
        return getPeriodOfValidityRange(day, validityDate);
    }

    @Override
    public PeriodOfValidityBO.Builder getPeriodOfValidityRange(Integer day, Date validityDate) {
        if (ObjectUtils.isEmpty(validityDate)) {
            return null;
        }
        final ZonedDateTime creatDate = validityDate.toInstant().atZone(ZoneId.systemDefault());
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
            throws IllegalArgumentException {
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
            throws IllegalArgumentException {
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
            , Supplier<Date> validityDateSupplier) throws IllegalArgumentException {
        final Object o = validityDayStrSupplier.get();
        if (o instanceof String) {
            return getPeriodOfValidityRange((String) o, validityDateSupplier.get());
        } else if (o instanceof Integer) {
            return getPeriodOfValidityRange((Integer) o, validityDateSupplier.get());
        } else {
            throw new IllegalArgumentException("暂时只接受“String”或“Integer”数据类型的结果");
        }
    }

    private Date getAppletTimeBySyncUser(MarketingSyncUser syncUser) {
        MarketingSyncUser user = marketingSyncUserMapper.getAppletTimeBySyncUser(syncUser);
        return ObjectUtils.isEmpty(user) ? null : (user.getAppletTime() == null
                ? (user.getCreateTime() == null
                ? null : user.getCreateTime()) : user.getAppletTime());
    }

    @Override
    public Result<Boolean> configValidDateDefault(MarketingSyncUser syncUser) {
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(false);
        String appletDate = syncUser.getAppletDate();
        MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
        example.createCriteria()
                .andApiCodeEqualTo(syncUser.getApiCode())
                .andUserTypeEqualTo(syncUser.getUserType())
                .andAppletDateEqualTo(appletDate)
                .andValidTypeEqualTo(1)
                .andIsDelEqualTo(1);
        // 检查db中是否已经存在有效期记录
        int count = marketingDataValidConfigMapper.countByExample(example);
        if (count > 0) {
            return result;
        }
        MarketingDataValidConfig newDataValidConfig = new MarketingDataValidConfig();
        newDataValidConfig.setApiCode(syncUser.getApiCode());
        newDataValidConfig.setUserType(syncUser.getUserType());
        newDataValidConfig.setAppletDate(appletDate);
        newDataValidConfig.setIsDel(1);
        newDataValidConfig.setValidType(1);
        newDataValidConfig.setCreateTime(new Date());
        newDataValidConfig.setUpdateTime(newDataValidConfig.getCreateTime());
        newDataValidConfig.setValidStartDate(appletDate);
        MarketingDataValidConfigDefaultExample exampleConfig = new MarketingDataValidConfigDefaultExample();
        exampleConfig.createCriteria()
                .andApiCodeEqualTo(syncUser.getApiCode())
                .andUserTypeEqualTo(syncUser.getUserType())
                .andIsDelEqualTo(1);
        exampleConfig.setOrderByClause("create_time DESC limit 1");
        // 查询默认有效期生成配置表
        List<MarketingDataValidConfigDefault> configDefaults = marketingDataValidConfigDefaultMapper.selectValidDaysByExample(
                exampleConfig);
        Integer days;
        // 根据配置表计算默认的有效期范围,未在生成配置表中的默认为准永久有效，
        if (configDefaults.size() < 1 || (days = configDefaults.get(0).getValidDaysDefault()) == null) {
            MarketingDataValidConfigDefaultExample exampleDefaultConfig = new MarketingDataValidConfigDefaultExample();
            exampleDefaultConfig.createCriteria().andApiCodeIn(Arrays.asList("defaultConfig", syncUser.getApiCode()))
                    .andUserTypeEqualTo("defaultConfig").andIsDelEqualTo(9);
            exampleDefaultConfig.setOrderByClause("api_code");
            List<MarketingDataValidConfigDefault> defaults = marketingDataValidConfigDefaultMapper.selectByExample(
                    exampleDefaultConfig);
            if (defaults.size() < 1) {
                // 设置准永久有效，该值可根据数据库中可接受的数据范围设定
                newDataValidConfig.setValidEndDate("9999-12-31");
            } else {
                // 1.可配置初始有效期配置，apiCode与userType的默认值都为defaultConfig；
                // 2.可自定义apiCode，但userType的默认值都为defaultConfig
                // 3.配置均为失效状态
                Map<String, MarketingDataValidConfigDefault> defaultMap = defaults.stream().collect(
                        Collectors.toMap(MarketingDataValidConfigDefault::getApiCode, Function.identity()
                                , BinaryOperator.maxBy(Comparator.comparing(MarketingDataValidConfigDefault::getCreateTime))));
                MarketingDataValidConfigDefault dataValidConfigDefault = new MarketingDataValidConfigDefault();
                dataValidConfigDefault.setValidDaysDefault(30);
                MarketingDataValidConfigDefault defaultConfig = defaultMap.getOrDefault(syncUser.getApiCode()
                        , defaultMap.getOrDefault("defaultConfig", dataValidConfigDefault));
                String newDateStr = LocalDate.parse(appletDate).plusDays(defaultConfig.getValidDaysDefault()).toString();
                newDataValidConfig.setValidEndDate(newDateStr);
            }
        } else {
            String newDateStr = LocalDate.parse(appletDate).plusDays(days).toString();
            newDataValidConfig.setValidEndDate(newDateStr);
        }
        // 将默认有效期内容持久化到db
        int i = marketingDataValidConfigMapper.insertSelective(newDataValidConfig);
        if (i < 1) {
            log.error("生成默认有效期入库失败！apiCode:{},userType:{},appletDate:{}"
                    , syncUser.getApiCode(), syncUser.getUserType(), appletDate);
            result.setDate(true);
        }
        return result;
    }

    @Override
    public Result<Boolean> customizeConfigValidDateDefault(MarketingSyncUser syncUser) {
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(false);
        this.configValidDateDefault(syncUser);
        MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
        example.createCriteria()
                .andApiCodeEqualTo(syncUser.getApiCode())
                .andUserTypeEqualTo(syncUser.getUserType())
                .andAppletDateEqualTo(syncUser.getAppletDate())
                .andValidTypeEqualTo(1)
                .andIsDelEqualTo(1);
        // 检查db中是否已经存在有效期记录
        List<MarketingDataValidConfig> marketingDataValidConfigs = marketingDataValidConfigMapper.selectByExample(example);
        // 插入子表
        for (MarketingDataValidConfig marketingDataValidConfig : marketingDataValidConfigs) {
            // 查询子表是否已经生成有效期
            MarketingCustomizeDataValidConfigExample marketingCustomizeDataValidConfigExample =
                    new MarketingCustomizeDataValidConfigExample();
            marketingCustomizeDataValidConfigExample.createCriteria()
                    .andApiCodeEqualTo(syncUser.getApiCode())
                    .andUserTypeEqualTo(syncUser.getUserType())
                    .andTaskIdEqualTo(syncUser.getCusBatch())
                    .andAppletDateEqualTo(syncUser.getAppletDate())
                    .andIsDelEqualTo(1);
            int i = marketingCustomizeDataValidConfigMapper.countByExample(marketingCustomizeDataValidConfigExample);
            if (i == 0) {
                // 插入定制表
                MarketingCustomizeDataValidConfig marketingCustomizeDataValidConfig =
                        getMarketingCustomizeDataValidConfig(syncUser, marketingDataValidConfig);
                int j = marketingCustomizeDataValidConfigMapper.insertSelective(marketingCustomizeDataValidConfig);
                if (j < 1) {
                    log.error("生成默认定制有效期入库失败！apiCode:{},userType:{},taskId:{}"
                            , syncUser.getApiCode(), syncUser.getUserType(), syncUser.getCusBatch());
                }
            }
        }
        return result;
    }

    private static MarketingCustomizeDataValidConfig getMarketingCustomizeDataValidConfig(MarketingSyncUser syncUser,
                                                                                          MarketingDataValidConfig marketingDataValidConfig) {
        Long dataValidConfigId = marketingDataValidConfig.getId();
        MarketingCustomizeDataValidConfig marketingCustomizeDataValidConfig = new MarketingCustomizeDataValidConfig();
        marketingCustomizeDataValidConfig.setApiCode(syncUser.getApiCode());
        marketingCustomizeDataValidConfig.setDataValidConfigId(dataValidConfigId);
        marketingCustomizeDataValidConfig.setAppletDate(syncUser.getAppletDate());
        marketingCustomizeDataValidConfig.setTaskId(syncUser.getCusBatch());
        marketingCustomizeDataValidConfig.setValidStartDate(marketingDataValidConfig.getValidStartDate());
        marketingCustomizeDataValidConfig.setValidEndDate(marketingDataValidConfig.getValidEndDate());
        marketingCustomizeDataValidConfig.setUserType(marketingDataValidConfig.getUserType());
        marketingCustomizeDataValidConfig.setCreateTime(new Date());
        marketingCustomizeDataValidConfig.setUpdateTime(new Date());
        return marketingCustomizeDataValidConfig;
    }

}
