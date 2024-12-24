package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
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
    @Transactional(rollbackFor = Exception.class)
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
        if (syncUser.getStatus() != null && MonitorTypeEnum.STATUS_2.getTypeCode() == syncUser.getStatus()) {
            example.setOrderByClause(" id for update");
        }
        // 检查db中是否已经存在有效期记录
        int count = marketingDataValidConfigMapper.countByExample(example);
        if (count > 0) {
            return result;
        }
        MarketingDataValidConfigDefaultExample exampleConfig = new MarketingDataValidConfigDefaultExample();
        exampleConfig.createCriteria()
                .andApiCodeEqualTo(syncUser.getApiCode())
                .andUserTypeEqualTo(syncUser.getUserType())
                .andIsDelEqualTo(1);
        if (syncUser.getStatus() != null && MonitorTypeEnum.STATUS_2.getTypeCode() == syncUser.getStatus()) {
            exampleConfig.setOrderByClause("create_time DESC limit 1 for update");
        } else {
            exampleConfig.setOrderByClause("create_time DESC limit 1");
        }
        // 查询默认有效期生成配置表
        List<MarketingDataValidConfigDefault> configDefaults = marketingDataValidConfigDefaultMapper
                .selectValidDaysByExample(exampleConfig);
        MarketingDataValidConfig newDataValidConfig = new MarketingDataValidConfig();
        newDataValidConfig.setApiCode(syncUser.getApiCode());
        newDataValidConfig.setUserType(syncUser.getUserType());
        newDataValidConfig.setAppletDate(appletDate);
        newDataValidConfig.setIsDel(1);
        newDataValidConfig.setValidType(1);
        newDataValidConfig.setCreateTime(new Date());
        newDataValidConfig.setUpdateTime(newDataValidConfig.getCreateTime());
        newDataValidConfig.setValidStartDate(appletDate);
        Integer days;
        // 根据配置表计算默认的有效期范围,使用默认有效期配置中默认的配置项defaultConfig
        if (configDefaults.size() < 1 || (days = configDefaults.get(0).getValidDaysDefault()) == null) {
            MarketingDataValidConfigDefaultExample exampleDefaultConfig = new MarketingDataValidConfigDefaultExample();
            exampleDefaultConfig.createCriteria().andApiCodeIn(Arrays.asList("defaultConfig", syncUser.getApiCode()))
                    .andUserTypeEqualTo("defaultConfig").andIsDelEqualTo(1);
            exampleDefaultConfig.setOrderByClause("api_code");
            List<MarketingDataValidConfigDefault> defaults = marketingDataValidConfigDefaultMapper.selectByExample(
                    exampleDefaultConfig);
            if (defaults.size() < 1) {
                // 设置准永久有效，该值可根据数据库中可接受的数据范围设定
                newDataValidConfig.setValidEndDate("9999-12-31");
            } else {
                // 1.可配置初始有效期配置，apiCode与userType的默认值都为defaultConfig；
                // 2.可自定义apiCode，但userType的默认值都为defaultConfig
                Map<String, MarketingDataValidConfigDefault> defaultMap = defaults.stream().collect(
                        Collectors.toMap(MarketingDataValidConfigDefault::getApiCode, Function.identity()
                                , BinaryOperator.maxBy(Comparator.comparing(MarketingDataValidConfigDefault::getCreateTime))));
                MarketingDataValidConfigDefault dataValidConfigDefault = new MarketingDataValidConfigDefault();
                dataValidConfigDefault.setValidDaysDefault(30);
                MarketingDataValidConfigDefault defaultConfig = defaultMap.getOrDefault(syncUser.getApiCode()
                        , defaultMap.getOrDefault("defaultConfig", dataValidConfigDefault));
                String newDateStr = LocalDate.parse(appletDate).plusDays(defaultConfig.getValidDaysDefault()).toString();
                newDataValidConfig.setValidEndDate(newDateStr);
                // 添加默认配置
                MarketingDataValidConfigDefault newConfigDefault = new MarketingDataValidConfigDefault();
                newConfigDefault.setApiCode(syncUser.getApiCode());
                newConfigDefault.setValidDaysDefault(defaultConfig.getValidDaysDefault());
                newConfigDefault.setUserType(syncUser.getUserType());
                newConfigDefault.setIsDel(1);
                newConfigDefault.setCreateTime(new Date());
                newConfigDefault.setUpdateTime(newConfigDefault.getCreateTime());
                marketingDataValidConfigDefaultMapper.insertSelective(newConfigDefault);
            }
        } else {
            String newDateStr;
            MarketingDataValidConfigDefault configDefault = configDefaults.get(0);
            if (configDefault.getValidType().equals(0)) {
                newDateStr = LocalDate.parse(appletDate).plusDays(days).toString();
            } else {// 按月维度
                newDateStr = LocalDate.now().plusMonths(configDefault.getValidDaysDefault()).with(TemporalAdjusters.lastDayOfMonth()).toString();
            }
            newDataValidConfig.setValidEndDate(newDateStr);
        }
        // 将默认有效期内容持久化到db
        int i = marketingDataValidConfigMapper.insertSelective(newDataValidConfig);
        if (i < 1) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(),
                    "生成默认有效期入库失败！apiCode:" + syncUser.getApiCode()
                            + ",userType:" + syncUser.getUserType() + ",appletDate:" + appletDate));
            result.setDate(true);
        }
        return result;
    }

    @Override
    public Result<Boolean> customizeConfigValidDateDefault(MarketingSyncUser syncUser) {
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(false);
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
                        getMarketingCustomizeDataValidConfig(syncUser);
                int j = marketingCustomizeDataValidConfigMapper.insertSelective(marketingCustomizeDataValidConfig);
                if (j < 1) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(),
                            "生成默认定制有效期入库失败！apiCode:" + syncUser.getApiCode() + ",userType:" + syncUser.getUserType()
                                    + ",taskId:" + syncUser.getCusBatch()));
                }else {
                    try {
                    //  更新通用有效期配置表
                    MarketingDataValidConfig mc = new MarketingDataValidConfig();
                    mc.setValidStartDate(marketingCustomizeDataValidConfig.getValidStartDate());
                    mc.setValidEndDate(marketingCustomizeDataValidConfig.getValidEndDate());
                    MarketingDataValidConfigExample mcExample = new MarketingDataValidConfigExample();
                    mcExample.createCriteria().andApiCodeEqualTo(marketingCustomizeDataValidConfig.getApiCode())
                                    .andAppletDateEqualTo(marketingCustomizeDataValidConfig.getAppletDate())
                                    .andUserTypeEqualTo(marketingCustomizeDataValidConfig.getUserType())
                                    .andIsDelEqualTo(1);
                        marketingDataValidConfigMapper.updateByExampleSelective(mc,mcExample);
                    }catch (Exception e){
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(),
                                "奇富360 有效期变更 更新通用表数据失败，不影响业务使用，研发人员需要关注!"), e);
                    }

                }
            }
        return result;
    }

    @Override
    public Result<Boolean> generateConfigValidByStartAndEndDate(MarketingSyncUser syncUser) {
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(false);
        // 查询有效期配置表是否已经生成有效期
        MarketingDataValidConfigExample validConfigExample = new MarketingDataValidConfigExample();
        validConfigExample.createCriteria()
                .andApiCodeEqualTo(syncUser.getApiCode())
                .andUserTypeEqualTo(syncUser.getUserType())
                .andAppletDateEqualTo(syncUser.getAppletDate())
                .andIsDelEqualTo(1);
        int i = marketingDataValidConfigMapper.countByExample(validConfigExample);
        if (i > 0) {
            return result;
        }

        MarketingSyncUser marketingSyncByCusBatch = marketingSyncUserMapper.getMarketingSyncByAppletDateAndUserType(
                syncUser.getApiCode(),
                syncUser.getUserType(),
                syncUser.getAppletDate());

        if (Objects.isNull(marketingSyncByCusBatch)) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(),
                    "根据有效期开始和结束时间生成有效期范围，没有查到上传数据，研发人员需要排查!"));
            return result;
        }
        JSONObject json = JSON.parseObject(marketingSyncByCusBatch.getReserveField1());
        String validStartDate = json.getString("validStartDate");
        String validEndDate = json.getString("validEndDate");

        MarketingDataValidConfig validConfig = new MarketingDataValidConfig();
        validConfig.setApiCode(syncUser.getApiCode());
        validConfig.setUserType(syncUser.getUserType());
        validConfig.setAppletDate(syncUser.getAppletDate());
        validConfig.setIsDel(1);
        validConfig.setValidType(1);
        validConfig.setCreateTime(new Date());
        validConfig.setUpdateTime(new Date());
        validConfig.setValidStartDate(validStartDate);
        validConfig.setValidEndDate(validEndDate);

        marketingDataValidConfigMapper.insertSelective(validConfig);
        result.setDate(true);
        return new Result<>();
    }

    private  MarketingCustomizeDataValidConfig getMarketingCustomizeDataValidConfig(MarketingSyncUser syncUser) {
        // 查询当前 api_code ,task_id ,applet_date下的 上传数据获取其中一条解析，reserve_field1 下的开始时间和结束时间
        // reserve_field1: {"operationScene":"creditT30","expireDate":"2024-07-12 23:59:59",
        // "userType":"1","custGroupName":"T0其他渠道低质量","effectiveDate":"2024-07-06 00:00:00"}
        MarketingSyncUser marketingSyncByCusBatch = marketingSyncUserMapper.getMarketingSyncByCusBatch(
                syncUser.getApiCode(),
                syncUser.getCusBatch(),
                syncUser.getUserType(),
                syncUser.getAppletDate());
        MarketingCustomizeDataValidConfig marketingCustomizeDataValidConfig = new MarketingCustomizeDataValidConfig();
        marketingCustomizeDataValidConfig.setApiCode(syncUser.getApiCode());
        marketingCustomizeDataValidConfig.setAppletDate(syncUser.getAppletDate());
        marketingCustomizeDataValidConfig.setTaskId(syncUser.getCusBatch());
        try {
            // reserve_field1: {"operationScene":"creditT30","expireDate":"2024-07-12 23:59:59",
            // "userType":"1","custGroupName":"T0其他渠道低质量","effectiveDate":"2024-07-06 00:00:00"}
            JSONObject json = JSON.parseObject(marketingSyncByCusBatch.getReserveField1());
            String effectiveDate = json.getString("effectiveDate");
            String expireDate = json.getString("expireDate");
            log.warn("查询的上传输数据信息effectiveDate：{}",effectiveDate);
            log.warn("查询的上传输数据信息expireDate：{}",expireDate);
            marketingCustomizeDataValidConfig.setValidStartDate(DateFormat(effectiveDate));
            marketingCustomizeDataValidConfig.setValidEndDate(DateFormat(expireDate));
            if(StringUtils.isEmpty(effectiveDate) || StringUtils.isEmpty(expireDate)){
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(),
                        "奇富360生成有效期时，解析reserve_field1 并获取开始时间和结束时间失败：上传数据的api_code:" + marketingSyncByCusBatch.getApiCode()
                                + ",id:" + marketingSyncByCusBatch.getId() + ",taskId(cus_batch):" + marketingSyncByCusBatch.getCusBatch()
                                + ",reserve_field1:" + marketingSyncByCusBatch.getReserveField1()));
            }
        }catch (Exception e){
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(),
                    "奇富360生成有效期时，解析reserve_field1 并获取开始时间和结束时间失败：上传数据的api_code:" + marketingSyncByCusBatch.getApiCode()
                            + ",id:" + marketingSyncByCusBatch.getId() + ",taskId(cus_batch):" + marketingSyncByCusBatch.getCusBatch()
                            + ",reserve_field1:" + marketingSyncByCusBatch.getReserveField1()), e);
        }
        marketingCustomizeDataValidConfig.setUserType(syncUser.getUserType());
        marketingCustomizeDataValidConfig.setCreateTime(new Date());
        marketingCustomizeDataValidConfig.setUpdateTime(new Date());
        return marketingCustomizeDataValidConfig;
    }

    /**
     * 日期格式化
     * @return
     */
    public String DateFormat(String DateStr) throws ParseException {
        if (StringUtils.isEmpty(DateStr)) {
            return "";
        }
        SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parse = formate.parse(DateStr);
        SimpleDateFormat sdf = new SimpleDateFormat("", Locale.SIMPLIFIED_CHINESE);
        sdf.applyPattern("yyyy-MM-dd");
        return sdf.format(parse);
    }

}
