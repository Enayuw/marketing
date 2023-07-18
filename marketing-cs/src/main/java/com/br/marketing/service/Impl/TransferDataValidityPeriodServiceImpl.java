package com.br.marketing.service.Impl;

import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.CellValidityPeriodBO;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.util.PeriodOfValidityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/14 14:49
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TransferDataValidityPeriodServiceImpl implements TransferDataValidityPeriodService {

    private final static String DATEFORMATPATTERN = "yyyy-MM-dd";


    private final MarketingSyncUserMapper marketingSyncUserMap;

    private final MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    private final IPeriodOfValidityService iPeriodOfValidityService;

    private final MarketingSyncUserMapper marketingSyncUserMapper;

    private final static DateTimeFormatter DATE_FORMAT_PATTERN = DateTimeFormatter.ofPattern(DATEFORMATPATTERN);

    @Override
    public MarketingSyncUser getNewValidityPeriodData(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        return getMarketingSyncUser(marketingTransferSyncUser, requestDate);
    }
    @Override
    public MarketingSyncUser getNewValidityPeriodDataFirstVersion(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        return getMarketingSyncUserFistVersion(marketingTransferSyncUser, requestDate);
    }
    @Override
    public boolean isValidityPeriodFirstVersion(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        return getMarketingSyncUserFistVersion(marketingTransferSyncUser, requestDate) != null;
    }

    @Override
    public boolean isValidityPeriod(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        return getMarketingSyncUser(marketingTransferSyncUser, requestDate) != null;
    }


    @Override
    public MarketingTransferSyncUserCell getNewValidityPeriodTransferData(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        MarketingSyncUser marketingSyncUser = getMarketingSyncUser(marketingTransferSyncUser, requestDate);
        if (marketingSyncUser != null) {
            MarketingTransferSyncUserCell marketingTransferSyncUserCell = new MarketingTransferSyncUserCell();
            BeanUtils.copyProperties(marketingTransferSyncUser, marketingTransferSyncUserCell);
            marketingTransferSyncUserCell.setCell(marketingSyncUser.getCell());
            marketingTransferSyncUserCell.setTaskId(marketingSyncUser.getCusBatch());
            marketingTransferSyncUserCell.setUserType(marketingSyncUser.getUserType());
            return marketingTransferSyncUserCell;
        }
        return null;
    }

    @Override
    public Result<Date> getValidityBeginOfTn(String apiCode, Date endDate) {
        MarketingDataValidConfigExample configExample = new MarketingDataValidConfigExample();
        configExample.createCriteria().andApiCodeEqualTo(apiCode).andValidTypeEqualTo(2).andIsDelEqualTo(1);
        List<MarketingDataValidConfig> marketingDataValidConfigs = marketingDataValidConfigMapper.selectByExample(configExample);
        if (marketingDataValidConfigs.size() > 0) {
            MarketingDataValidConfig marketingDataValidConfig = marketingDataValidConfigs.get(0);
            Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(marketingDataValidConfig.getValidDays());
            PeriodOfValidityBO builder = iPeriodOfValidityService.getPeriodOfValidityRange(-day, endDate).builder();
            Date beginDate = builder.getBeginDate();
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(beginDate);
        } else {
            String minAppletDate = marketingSyncUserMap.getMinAppletDate(apiCode);
            if (StringUtils.isBlank(minAppletDate)) {
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该客户未上传过数据");
            }
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(minAppletDate);

        }
    }


    /**
     * shijian
     */
    private MarketingSyncUser getMarketingSyncUser(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        MarketingSyncUser marketingSyncUser = null;
        // 1. 查询配置表

        List<MarketingDataValidConfig> marketingDataValidConfigs = marketingDataValidConfigMapper.selectInfo(marketingTransferSyncUser.getApiCode(), marketingTransferSyncUser.getUserType());
        // 获取需要判断的指定日期
        requestDate = requestDate == null ? marketingTransferSyncUser.getRequestData() : requestDate;
        LocalDate parse = LocalDate.parse(requestDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));

        // 2. 获取T,N 模式下 有效期范围的规则集合，T，N
        List<MarketingDataValidConfig> marketingDataValidConfigTN = marketingDataValidConfigs.stream().filter(m -> m.getValidType() == 1).collect(Collectors.toList());
        List<MarketingDataValidConfig> collectRequestDateTN = new ArrayList<>();
        marketingDataValidConfigTN.forEach(mctn -> {
            String validStartDate = mctn.getValidStartDate();
            String validEndDate = mctn.getValidEndDate();
            LocalDate startDate = LocalDate.parse(validStartDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            LocalDate endDate = LocalDate.parse(validEndDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            if ((startDate.isBefore(parse) || startDate.isEqual(parse))
                    && (parse.isBefore(endDate) || parse.isEqual(endDate))) {
                collectRequestDateTN.add(mctn);
            }
        });

        // 如果T,N 模式不为空则查询最新一条数据
        if (collectRequestDateTN.size() > 0) {

            marketingSyncUser = marketingSyncUserMap.selectInAppletDate(collectRequestDateTN, marketingTransferSyncUser);
        }

        //3. 获取【非】以上集合最新的一条数据 configAppletDateTN 需要进行非空判断
        MarketingSyncUser marketingSyncUserTaN = marketingSyncUserMap.selectNotInAppletDate(marketingDataValidConfigTN, marketingTransferSyncUser);
        if (marketingSyncUserTaN == null) {
            return marketingSyncUser;
        }
        // 4. 获取T+N有效期范围的规则集合，T+N
        List<MarketingDataValidConfig> marketingDataValidConfigTaN = marketingDataValidConfigs.stream().filter(m -> m.getValidType() == 2).collect(Collectors.toList());

        //5. 空为没有配置默认永久有效
        if (marketingDataValidConfigTaN.size() == 0) {
            marketingSyncUser = getNewMarketingSyncUser(marketingSyncUser, marketingSyncUserTaN);
        } else {
            //6. 非空 判断上传数据的有效期。
            LocalDate applyDate = LocalDate.parse(marketingSyncUserTaN.getAppletDate(), DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            for (MarketingDataValidConfig mctan : marketingDataValidConfigTaN) {
                // 判断是否有效
                if (iPeriodOfValidityService.isNotExpire(convertToDateViaInstant(parse), mctan.getValidDays(), convertToDateViaInstant(applyDate))) {
                    marketingSyncUser = getNewMarketingSyncUser(marketingSyncUser, marketingSyncUserTaN);
                    break;
                }
            }
        }
        return marketingSyncUser;
    }

    public java.util.Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay().atZone(ZoneId.systemDefault())
                .toInstant());
    }


    private static MarketingSyncUser getNewMarketingSyncUser(MarketingSyncUser marketingSyncUser, MarketingSyncUser marketingSyncUserTaN) {
        if (marketingSyncUser != null) {
            Date appletTimeTN = marketingSyncUser.getAppletTime();
            Date appletTimeTaN = marketingSyncUserTaN.getAppletTime();
            if (appletTimeTaN.after(appletTimeTN)) {
                marketingSyncUser = marketingSyncUserTaN;
            }
        } else {
            marketingSyncUser = marketingSyncUserTaN;
        }
        return marketingSyncUser;
    }

    @Override
    public Map<String, SyncUserValidityPeriodBO> getSyncUserValidityPeriodMap(
            List<MarketingTransferSyncUser> transferSyncUserList, String apiCode) {
        // 转化数据CustNum案件编号及对应的UserType场景
        Set<String> map = transferSyncUserList.parallelStream().map(MarketingTransferSyncUser::getCustNum)
                .collect(Collectors.toSet());
        List<MarketingSyncUser> preUserByTask = marketingSyncUserMapper.getSyncUserLastByCustNumsAndStatus(apiCode, map);
        // apicode全量有效期配置
        List<MarketingDataValidConfig> configList = findConfigAllByApiCodeList(apiCode);
        // 未配置任何有效期
        if (CollectionUtils.isEmpty(configList)) {
            return longValid(preUserByTask);
        }

        // 配置了T,T （范围）的情况
        // TODO: 2023-03-22  T,T （范围）暂时不做, map.valeus 为转化场景集合
//        final Collection<String> userTypes = map.values();
//        ttValidityPeriodMap(configList, preUserByTask, userTypes);

        // 配置了T+N的情况
        return tnValidityPeriodMap(configList, preUserByTask);
    }

    /**
     * 2023-03-23 12:25
     * 配置T,T（范围），原始数据有效期
     */
    private Map<String, SyncUserValidityPeriodBO> ttValidityPeriodMap(List<MarketingDataValidConfig> configList
            , List<MarketingSyncUser> preUserByTask, final Collection<String> userTypes) {
        List<MarketingDataValidConfig> ttList = configList.parallelStream().filter(config -> config.getValidType()
                .equals(1) && userTypes.contains(config.getUserType())).collect(Collectors.toList());
        return null;
    }

    /**
     * 2023-03-23 12:25
     * 配置T+N，原始数据有效期
     */
    private Map<String, SyncUserValidityPeriodBO> tnValidityPeriodMap(List<MarketingDataValidConfig> configList
            , List<MarketingSyncUser> preUserByTask) {
        List<MarketingDataValidConfig> tnList = configList.stream().filter(
                config -> config.getValidType().equals(2)).collect(Collectors.toList());
        // 未配置T+N
        if (CollectionUtils.isEmpty(tnList)) {
            return longValid(preUserByTask);
        }

        final Date date = new Date();
        // 缓存案件的有效期配置
        final Map<String, MarketingDataValidConfig> configMap = new ConcurrentHashMap<>(2048);
        // 处理T+N的配置
        ConcurrentMap<String, SyncUserValidityPeriodBO> boMap = preUserByTask.parallelStream().collect(Collectors.toConcurrentMap(
                // 去重，取最新
                MarketingSyncUser::getCustNum, Function.identity(), this::latestMarketingSyncUser)).values()
                .parallelStream().filter(user -> {
                    // 遍历检查是否在有效期
                    for (MarketingDataValidConfig config : tnList) {
                        // 只要满足有效期就立即返回
                        if (iPeriodOfValidityService.isNotExpire(date, config.getValidDays(), user.getAppletTime())) {
                            // 缓存案件对应的有效期配置
                            configMap.put(user.getCustNum(), config);
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toConcurrentMap(
                        MarketingSyncUser::getCustNum, syncUser -> {
                            // 组装原始数据有效期
                            SyncUserValidityPeriodBO bo = new SyncUserValidityPeriodBO();
                            MarketingDataValidConfig marketingDataValidConfig = configMap.get(syncUser.getCustNum());
                            PeriodOfValidityBO.Builder periodOfValidityRange = iPeriodOfValidityService.getPeriodOfValidityRange(
                                    marketingDataValidConfig.getValidDays(), ObjectUtils.isEmpty(syncUser.getAppletTime())
                                            ? syncUser.getCreateTime()
                                            : syncUser.getAppletTime());
                            bo.setSyncUser(syncUser);
                            bo.setBuilder(periodOfValidityRange);
                            return bo;
                        }));
        // 辅助 GC
        configMap.clear();
        preUserByTask.clear();
        return boMap;
    }

    /**
     * 2023-03-22 18:12
     * 永久有效
     *
     * @param preUserByTask 原始数据集合
     * @return Map key：custNum value：SyncUserValidityPeriodBO {@linkplain SyncUserValidityPeriodBO MarketingSyncUser PeriodOfValidityBO.Builder}
     */
    private Map<String, SyncUserValidityPeriodBO> longValid(List<MarketingSyncUser> preUserByTask) {
        return preUserByTask.parallelStream().collect(Collectors.toConcurrentMap(
                MarketingSyncUser::getCustNum, marketingSyncUser -> {
                    SyncUserValidityPeriodBO bo = new SyncUserValidityPeriodBO();
                    bo.setSyncUser(marketingSyncUser);
                    bo.setBuilder(PeriodOfValidityBO.custom(marketingSyncUser.getAppletTime(), null));
                    return bo;
                }, this::latestSyncUserValidityPeriodBO));
    }

    /**
     * 2023-03-22 18:19
     * 获取最新
     */
    private synchronized SyncUserValidityPeriodBO latestSyncUserValidityPeriodBO(SyncUserValidityPeriodBO v1, SyncUserValidityPeriodBO v2) {
        MarketingSyncUser syncUserV1 = v1.getSyncUser();
        MarketingSyncUser syncUserV2 = v2.getSyncUser();
        return latestMarketingSyncUser(syncUserV1, syncUserV2) == syncUserV1 ? v1 : v2;
    }

    /**
     * 2023-03-22 18:19
     * 获取最新：
     * 1、 v1的AppletTime不为null时，v2的AppletTime不为null，通过比较大小返回最大时间的对象
     * 2、 v1的AppletTime不为null时，v2的AppletTime为null，返回v1
     * 3、 v1的AppletTime为null时，v2的AppletTime不为null，返回v2
     * 4、 v1的AppletTime为null时，v2的AppletTime为null；v1的CreateTime不为null时，v2的CreateTime不为null，通过比较大小返回最大时间的对象
     * 4.1、 v1的AppletTime为null时，v2的AppletTime为null；v1的CreateTime不为null时，v2的CreateTime为null，返回v1
     * 4.2、 v1的AppletTime为null时，v2的AppletTime为null；v1的CreateTime为null时，v2的CreateTime不为null，返回v2
     * 4.3、 v1的AppletTime为null时，v2的AppletTime为null；v1的CreateTime为null时，v2的CreateTime为null，返回v1
     */
    private synchronized MarketingSyncUser latestMarketingSyncUser(MarketingSyncUser o, MarketingSyncUser o1) {
        return ObjectUtils.isEmpty(o) ? (ObjectUtils.isEmpty(o1) ? o : o1)
                : (ObjectUtils.isEmpty(o1) ? (ObjectUtils.isEmpty(o.getAppletTime())
                ? (ObjectUtils.isEmpty(o1.getAppletTime())
                ? (ObjectUtils.isEmpty(o.getCreateTime())
                ? (ObjectUtils.isEmpty(o1.getCreateTime())
                ? o : o1) : (ObjectUtils.isEmpty(o1.getCreateTime())
                ? o : (o.getCreateTime().compareTo(o1.getCreateTime()) > 0
                ? o : o1))) : o1) : (ObjectUtils.isEmpty(o1.getAppletTime())
                ? o : (o.getAppletTime().compareTo(o1.getAppletTime()) > 0
                ? o : o1))) : o1);
    }

    /**
     * 2023-03-23 12:38
     * apicode全量有效期配置
     */
    private List<MarketingDataValidConfig> findConfigAllByApiCodeList(String apiCode) {
        MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(1);
        example.setOrderByClause("create_time desc, update_time desc");
        return marketingDataValidConfigMapper.selectByExample(example);
    }


    @Override
    public Map<String, SyncUserValidityPeriodBO> getSyncUserValidityPeriodMap(
            List<MarketingTransferSyncUser> transferSyncUserList, String apiCode, Object requestDateObj) {
        // apicode全量有效期配置
        List<MarketingDataValidConfig> configList = findConfigAllByApiCodeList(apiCode);
        // 未配置任何有效期
        if (CollectionUtils.isEmpty(configList)) {
            Set<String> set = transferSyncUserList.parallelStream().map(
                    MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserLastByCustNumsAndStatus(
                    apiCode, set);
            return longValid(syncUserList);
        }
        // 获取转化数据的请求日期
        Set<Map.Entry<Date, List<MarketingTransferSyncUser>>> dateSet = getTransferDataRequestDate(
                transferSyncUserList, requestDateObj).entrySet();
        Map<String, SyncUserValidityPeriodBO> boMap = new ConcurrentHashMap<>(2048);
        // 按日期处理有效期
        for (Map.Entry<Date, List<MarketingTransferSyncUser>> dateListEntry : dateSet) {
            final Date requestDate = dateListEntry.getKey();
            final List<MarketingTransferSyncUser> transferSyncUsers = dateListEntry.getValue();
            // 获取T,T （范围）模式的配置记录
            List<MarketingDataValidConfig> ttDataValidConfigList = configList.parallelStream().filter(
                    config -> config.getValidType().equals(1) && org.apache.commons.lang3.StringUtils.isBlank(
                            config.getUserType())).collect(Collectors.toList());
            // 获取【非】以上集合最新的一条数据 configAppletDateTN 需要进行非空判断
            List<MarketingSyncUser> syncUserLastByNotInAppletDateList = marketingSyncUserMapper
                    .getSyncUserLastByNotInAppletDateList(apiCode, ttDataValidConfigList, transferSyncUserList);
            // 配置了T,T （范围）模式的情况
            List<MarketingSyncUser> ttSyncUserLastByInAppletDateList = ttSyncUserLastByInAppletDateList(apiCode,
                    ttDataValidConfigList, transferSyncUsers, requestDate);
            if (syncUserLastByNotInAppletDateList.size() == 0) {
                boMap.putAll(ttValidityPeriodMap(ttDataValidConfigList, ttSyncUserLastByInAppletDateList));
                continue;
            }
            Map<String, MarketingSyncUser> syncUserLastByNotInAppletDateMap = syncUserLastByNotInAppletDateList
                    .parallelStream().collect(Collectors.toConcurrentMap(MarketingSyncUser::getCustNum
                            , Function.identity(),(t1,t2)->t1));
            List<MarketingSyncUser> returnSyncUserList = ttSyncUserLastByInAppletDateList.parallelStream().filter(
                    syncUser -> !syncUserLastByNotInAppletDateMap.containsKey(syncUser.getCustNum()))
                    .collect(Collectors.toList());
            // 不在【非】集合中的数据
            boMap.putAll(ttValidityPeriodMap(ttDataValidConfigList, returnSyncUserList));
            Map<String, MarketingSyncUser> ttSyncUserLastByInAppletDateMap = ttSyncUserLastByInAppletDateList
                    .parallelStream().collect(Collectors.toConcurrentMap(MarketingSyncUser::getCustNum
                            , Function.identity(),(t1,t2)->t1));
            // 配置了T+N模式的情况
            boMap.putAll(tnValidityPeriodMap(configList, requestDate, ttSyncUserLastByInAppletDateMap
                    , syncUserLastByNotInAppletDateList));
        }
        return boMap;
    }

    @Override
    public Map<String, Map<String, SyncUserValidityPeriodBO>> getSyncUserValidityPeriodUserTypeMap(
            List<MarketingTransferSyncUser> transferSyncUserList, String apiCode) {
            return getSyncUserValidityPeriodUserTypeMap(transferSyncUserList, apiCode, null, null);
    }

    @Override
    public Map<String, Map<String, SyncUserValidityPeriodBO>> getSyncUserValidityPeriodUserTypeMap(List<MarketingTransferSyncUser> transferSyncUserList, String apiCode, String limitDate) {
            return getSyncUserValidityPeriodUserTypeMap(transferSyncUserList, apiCode, null, limitDate);
    }

    @Override
    public Map<String, Map<String, SyncUserValidityPeriodBO>> getSyncUserValidityPeriodUserTypeMap(
            List<MarketingTransferSyncUser> transferSyncUserList, String apiCode, Object requestDateObj, String UploadLimitDate) {
        // apicode全量有效期配置
        List<MarketingDataValidConfig> configList = findConfigAllByApiCodeList(apiCode);
        // 未配置任何有效期
        if (CollectionUtils.isEmpty(configList)) {
            Set<String> set = transferSyncUserList.parallelStream().map(
                    MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserLastByCustNumsAndStatusAndDate(
                    apiCode, set, UploadLimitDate);
            return longValidUserType(syncUserList);
        }
        // 获取转化数据的请求日期
        Set<Map.Entry<Date, List<MarketingTransferSyncUser>>> dateSet = getTransferDataRequestDate(
                transferSyncUserList, requestDateObj).entrySet();
        Map<String, Map<String, SyncUserValidityPeriodBO>> boMap = new ConcurrentHashMap<>(2048);
        // 按日期处理有效期
        for (Map.Entry<Date, List<MarketingTransferSyncUser>> dateListEntry : dateSet) {
            final Date requestDate = dateListEntry.getKey();
            final List<MarketingTransferSyncUser> transferSyncUsers = dateListEntry.getValue();
            // 获取T,T （范围）模式的配置记录
            List<MarketingDataValidConfig> ttDataValidConfigList = configList.parallelStream().filter(
                    config -> config.getValidType().equals(1) && org.apache.commons.lang3.StringUtils.isNotBlank(
                            config.getUserType())).collect(Collectors.toList());
            // 获取【非】以上集合最新的一条数据 configAppletDateTN 需要进行非空判断
            List<MarketingSyncUser> syncUserLastByNotInAppletDateList = marketingSyncUserMapper
                    .getSyncUserLastByNotInAppletDateUserTypeList(apiCode, ttDataValidConfigList, transferSyncUserList, UploadLimitDate);
            // 配置了T,T （范围）模式的情况
            List<MarketingSyncUser> ttSyncUserLastByInAppletDateList = ttSyncUserLastByInAppletDateAndUserTypeList(
                    apiCode, ttDataValidConfigList, transferSyncUsers, requestDate);
            if (syncUserLastByNotInAppletDateList.size() == 0) {
                boMap.putAll(ttValidityPeriodUserTypeMap(ttDataValidConfigList, ttSyncUserLastByInAppletDateList));
                continue;
            }
            Map<String, MarketingSyncUser> syncUserLastByNotInAppletDateMap = syncUserLastByNotInAppletDateList
                    .parallelStream().collect(Collectors.toConcurrentMap(MarketingSyncUser::getCustNum
                            , Function.identity(),(t1,t2)->t2));
            List<MarketingSyncUser> returnSyncUserList = ttSyncUserLastByInAppletDateList.parallelStream().filter(
                    syncUser -> !syncUserLastByNotInAppletDateMap.containsKey(syncUser.getCustNum()))
                    .collect(Collectors.toList());
            // 不在【非】集合中的数据
            boMap.putAll(ttValidityPeriodUserTypeMap(ttDataValidConfigList, returnSyncUserList));
            Map<String, MarketingSyncUser> ttSyncUserLastByInAppletDateMap = ttSyncUserLastByInAppletDateList
                    .parallelStream().collect(Collectors.toConcurrentMap(MarketingSyncUser::getCustNum
                            , Function.identity(),(t1,t2)->t2));
            // 配置了T+N模式的情况
            boMap.putAll(tnValidityPeriodUserTypeMap(configList, requestDate, ttSyncUserLastByInAppletDateMap
                    , syncUserLastByNotInAppletDateList));
        }
        return boMap;
    }


    /**
     * 2023-04-07 9:52
     * 获取数据的请求日期
     *
     * @return key requestDate; value List<MarketingTransferSyncUser>
     */
    private Map<Date, List<MarketingTransferSyncUser>> getTransferDataRequestDate(
            List<MarketingTransferSyncUser> transferSyncUserList, Object requestDateObj) {
        Map<Date, List<MarketingTransferSyncUser>> requestDateMap;
        if (requestDateObj == null) {
            // 对转化数据按请求日期分组
            requestDateMap = transferSyncUserList.parallelStream().collect(Collectors.groupingBy(user -> {
                try {
                    return DateUtils.parse(user.getRequestData(), DateHelper.LINE_DATE_FORMAT);
                } catch (ParseException pe) {
                    // 日期格式解析失败时，使用当前时间
                    try {
                        return Date.from(LocalDateTime.parse(user.getRequestTime(), DateTimeFormatter.ofPattern(
                                DateHelper.LINE_DATE_COLON_TIME_FORMAT_SSS)).toLocalDate().atStartOfDay(
                                ZoneId.systemDefault()).toInstant());
                    } catch (Exception e) {
                        try {
                            return Date.from(user.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                                    .toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        } catch (Exception exception) {
                            return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        }
                    }
                }
            }));
        } else {
            // 统一时间格式
            Date requestDate = switchDate(requestDateObj);
            requestDateMap = new ConcurrentHashMap<>(2);
            requestDateMap.put(requestDate, transferSyncUserList);
        }
        return requestDateMap;
    }

    /**
     * 2023-04-06 16:36
     * 转换日期
     * 支持数据格式 String(yyyy-MM-dd)、Date、LocalDate、LocalDateTime、Long、Calendar
     *
     * @param requestDateObj 请求日期对象
     * @return Date
     */
    private Date switchDate(Object requestDateObj) {
        Date requestDate;
        if (requestDateObj instanceof Date) {
            requestDate = (Date) requestDateObj;
        } else if (requestDateObj instanceof String) {
            try {
                requestDate = DateUtils.parse((String) requestDateObj, DateHelper.LINE_DATE_FORMAT);
            } catch (ParseException e) {
                requestDate = Date.from(LocalDateTime.parse((String) requestDateObj
                        , DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT_SSS))
                        .atZone(ZoneId.systemDefault()).toInstant());
            }
        } else if (requestDateObj instanceof LocalDate) {
            requestDate = Date.from(((LocalDate) requestDateObj).atStartOfDay().atZone(ZoneId.systemDefault())
                    .toInstant());
        } else if (requestDateObj instanceof LocalDateTime) {
            requestDate = Date.from(((LocalDateTime) requestDateObj).atZone(ZoneId.systemDefault()).toInstant());
        } else if (requestDateObj instanceof Long) {
            requestDate = new Date((Long) requestDateObj);
        } else if (requestDateObj instanceof Calendar) {
            requestDate = ((Calendar) requestDateObj).getTime();
        } else {
            throw new IllegalArgumentException("非法的参数：" + requestDateObj
                    + ",支持格式 String(yyyy-MM-dd)、Date、LocalDate、LocalDateTime、Long、Calendar");
        }
        return requestDate;
    }

    /**
     * 2023-03-23 12:25
     * 配置T,T（范围），原始数据
     */
    private List<MarketingSyncUser> ttSyncUserLastByInAppletDateList(String apiCode
            , List<MarketingDataValidConfig> configList
            , List<MarketingTransferSyncUser> transferSyncUserList, final Date requestDate) {
        // 获取包含请求日期的T,T （范围）模式的配置记录
        List<MarketingDataValidConfig> ttRequestDateDataValidConfigList = configList.parallelStream()
                .filter(config -> compareRequestDate(config, requestDate)).collect(Collectors.toList());
        // 包含请求日期的T,T （范围）模式的配置记录不为空则查询最新一条数据原始数据（上传数据）
        if (ttRequestDateDataValidConfigList.size() > 0) {
            return marketingSyncUserMapper.getSyncUserLastByInAppletDateList(apiCode
                    , ttRequestDateDataValidConfigList, transferSyncUserList);
        }
        return Collections.emptyList();
    }

    /**
     * 2023-03-23 12:25
     * 配置T,T（范围）与 userType，原始数据
     */
    private List<MarketingSyncUser> ttSyncUserLastByInAppletDateAndUserTypeList(
            String apiCode, List<MarketingDataValidConfig> configList
            , List<MarketingTransferSyncUser> transferSyncUserList, final Date requestDate) {
        final Set<String> userTypeSet = transferSyncUserList.stream().map(
                MarketingTransferSyncUser::getUserType).collect(Collectors.toSet());
        // 获取包含请求日期的T,T （范围）模式的配置记录
        List<MarketingDataValidConfig> ttRequestDateDataValidConfigList = configList.stream()
                .filter(config -> userTypeSet.contains(config.getUserType())
                        && compareRequestDate(config, requestDate)).collect(Collectors.toList());
        // 包含请求日期的T,T （范围）模式的配置记录不为空则查询最新一条数据原始数据（上传数据）
        if (ttRequestDateDataValidConfigList.size() > 0) {
            return marketingSyncUserMapper.getSyncUserLastByInAppletDateUserTypeList(apiCode
                    , ttRequestDateDataValidConfigList, transferSyncUserList);
        }
        return Collections.emptyList();
    }


    /**
     * 2023-04-10 17:47
     * 比较请求日期
     */
    private boolean compareRequestDate(MarketingDataValidConfig config, Date requestDate) {
        String validStartDate = config.getValidStartDate();
        String validEndDate = config.getValidEndDate();
        LocalDate startDate = LocalDate.parse(validStartDate, DATE_FORMAT_PATTERN);
        LocalDate endDate = LocalDate.parse(validEndDate, DATE_FORMAT_PATTERN);
        LocalDate localDate = requestDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return ((startDate.isBefore(localDate) || startDate.isEqual(localDate))
                && (localDate.isBefore(endDate) || localDate.isEqual(endDate)));
    }


    /**
     * 2023-03-23 12:25
     * 配置T+n，原始数据有效期
     */
    private Map<String, SyncUserValidityPeriodBO> tnValidityPeriodMap(List<MarketingDataValidConfig> configList
            , final Date requestDate
            , final Map<String, MarketingSyncUser> ttSyncUserLastByInAppletDateMap
            , List<MarketingSyncUser> syncUserLastByNotInAppletDateList) {
        // 获取包含请求日期的T+N模式的配置记录
        List<MarketingDataValidConfig> tnDataValidConfigList = configList.parallelStream().filter(
                config -> org.apache.commons.lang3.StringUtils.isBlank(config.getUserType())
                        && config.getValidType().equals(2)).collect(Collectors.toList());
        if (tnDataValidConfigList.size() == 0) {
            // 永久有效
            return longValid(syncUserLastByNotInAppletDateList.parallelStream().map(syncUser -> {
                String custNum = syncUser.getCustNum();
                return getNewMarketingSyncUser(ttSyncUserLastByInAppletDateMap.get(custNum), syncUser);
            }).collect(Collectors.toList()));
        } else {
            // 缓存案件的有效期配置
            final Map<String, MarketingDataValidConfig> configMap = new ConcurrentHashMap<>(1024);
            return syncUserLastByNotInAppletDateList.parallelStream().filter(syncUser -> {
                String custNum = syncUser.getCustNum();
                for (MarketingDataValidConfig config : tnDataValidConfigList) {
                    boolean bool = org.apache.commons.lang3.StringUtils.isBlank(config.getAppletDate())
                            || syncUser.getAppletDate().equals(config.getAppletDate());
                    // 判断是否有效
                    if (bool && iPeriodOfValidityService.isNotExpire(requestDate
                            , config.getValidDays(), syncUser.getAppletTime())) {
                        configMap.put(custNum, config);
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toConcurrentMap(MarketingSyncUser::getCustNum, syncUser -> {
                String custNum = syncUser.getCustNum();
                syncUser = getNewMarketingSyncUser(ttSyncUserLastByInAppletDateMap.get(custNum), syncUser);
                // 组装原始数据有效期
                SyncUserValidityPeriodBO bo = new SyncUserValidityPeriodBO();
                MarketingDataValidConfig marketingDataValidConfig = configMap.get(custNum);
                PeriodOfValidityBO.Builder periodOfValidityRange = iPeriodOfValidityService.getPeriodOfValidityRange(
                        marketingDataValidConfig.getValidDays(), ObjectUtils.isEmpty(syncUser.getAppletTime())
                                ? syncUser.getCreateTime() : syncUser.getAppletTime());
                bo.setSyncUser(syncUser);
                bo.setBuilder(periodOfValidityRange);
                return bo;
            }));
        }
    }

    /**
     * 2023-03-23 12:25
     * 配置T+n 与 userType，原始数据有效期
     */
    private Map<String, Map<String, SyncUserValidityPeriodBO>> tnValidityPeriodUserTypeMap(
            List<MarketingDataValidConfig> configList
            , final Date requestDate
            , final Map<String, MarketingSyncUser> ttSyncUserLastByInAppletDateMap
            , List<MarketingSyncUser> syncUserLastByNotInAppletDateList) {
        // 获取包含请求日期的T+N模式的配置记录
        List<MarketingDataValidConfig> tnDataValidConfigList = configList.parallelStream().filter(
                config -> org.apache.commons.lang3.StringUtils.isNotBlank(config.getUserType())
                        && config.getValidType().equals(3)).collect(Collectors.toList());
        if (tnDataValidConfigList.size() == 0) {
            // 永久有效
            return longValidUserType(syncUserLastByNotInAppletDateList.parallelStream().map(syncUser -> {
                String custNum = syncUser.getCustNum();
                return getNewMarketingSyncUser(ttSyncUserLastByInAppletDateMap.get(custNum), syncUser);
            }).collect(Collectors.toList()));
        } else {
            // 缓存案件的有效期配置
            final Map<String, MarketingDataValidConfig> configMap = new ConcurrentHashMap<>(1024);
            return syncUserLastByNotInAppletDateList.parallelStream().filter(syncUser -> {
                String custNum = syncUser.getCustNum();
                for (MarketingDataValidConfig config : tnDataValidConfigList) {
                    boolean bool = org.apache.commons.lang3.StringUtils.isBlank(config.getAppletDate())
                            || syncUser.getAppletDate().equals(config.getAppletDate());
                    // 判断是否有效
                    if (syncUser.getUserType().equals(config.getUserType()) && bool
                            && iPeriodOfValidityService.isNotExpire(
                            requestDate, config.getValidDays(), syncUser.getAppletTime())) {
                        configMap.put(custNum + config.getUserType(), config);
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.groupingBy(MarketingSyncUser::getCustNum))
                    .entrySet().parallelStream().collect(Collectors.toConcurrentMap(Map.Entry::getKey
                            , listEntry -> listEntry.getValue().parallelStream().collect(
                                    Collectors.toConcurrentMap(MarketingSyncUser::getUserType, syncUser -> {
                                        String custNum = syncUser.getCustNum();
                                        syncUser = getNewMarketingSyncUser(ttSyncUserLastByInAppletDateMap.get(custNum)
                                                , syncUser);
                                        // 组装原始数据有效期
                                        SyncUserValidityPeriodBO bo = new SyncUserValidityPeriodBO();
                                        MarketingDataValidConfig marketingDataValidConfig = configMap.get(
                                                custNum + syncUser.getUserType());
                                        PeriodOfValidityBO.Builder periodOfValidityRange = iPeriodOfValidityService
                                                .getPeriodOfValidityRange(marketingDataValidConfig.getValidDays()
                                                        , ObjectUtils.isEmpty(syncUser.getAppletTime())
                                                                ? syncUser.getCreateTime() : syncUser.getAppletTime());
                                        bo.setSyncUser(syncUser);
                                        bo.setBuilder(periodOfValidityRange);
                                        return bo;
                                    }, this::latestSyncUserValidityPeriodBO))));
        }
    }

    /**
     * 2023-03-23 12:25
     * 配置T,T（范围），原始数据有效期
     */
    private Map<String, SyncUserValidityPeriodBO> ttValidityPeriodMap(List<MarketingDataValidConfig> configList
            , final List<MarketingSyncUser> syncUserList) {
        return syncUserList.parallelStream().collect(
                Collectors.toConcurrentMap(MarketingSyncUser::getCustNum
                        , syncUser -> {
                            // 组装原始数据有效期
                            SyncUserValidityPeriodBO bo = new SyncUserValidityPeriodBO();
                            for (MarketingDataValidConfig config : configList) {
                                if (syncUser.getAppletDate().equals(config.getAppletDate())) {
                                    packageSyncUserValidityPeriodBO(bo, config);
                                    break;
                                }
                            }
                            bo.setSyncUser(syncUser);
                            return bo;
                        })
        );
    }

    /**
     * 2023-03-23 12:25
     * 配置T,T（范围）与 userType，原始数据有效期
     */
    private Map<String, Map<String, SyncUserValidityPeriodBO>> ttValidityPeriodUserTypeMap(
            List<MarketingDataValidConfig> configList, final List<MarketingSyncUser> syncUserList) {
        return syncUserList.parallelStream().collect(Collectors.groupingBy(MarketingSyncUser::getCustNum))
                .entrySet().parallelStream().collect(Collectors.toConcurrentMap(Map.Entry::getKey
                        , listEntry -> listEntry.getValue().parallelStream().collect(
                                Collectors.toConcurrentMap(MarketingSyncUser::getUserType, syncUser -> {
                                    // 组装原始数据有效期
                                    SyncUserValidityPeriodBO bo = new SyncUserValidityPeriodBO();
                                    for (MarketingDataValidConfig config : configList) {
                                        if (syncUser.getUserType().equals(config.getUserType())
                                                && syncUser.getAppletDate().equals(config.getAppletDate())) {
                                            packageSyncUserValidityPeriodBO(bo, config);
                                            break;
                                        }
                                    }
                                    bo.setSyncUser(syncUser);
                                    return bo;
                                }, this::latestSyncUserValidityPeriodBO)))
                );
    }

    /**
     * 2023-04-10 18:16
     * 组装日期范围
     */
    private void packageSyncUserValidityPeriodBO(SyncUserValidityPeriodBO bo, MarketingDataValidConfig config) {
        LocalDate startDate = LocalDate.parse(config.getValidStartDate(), DATE_FORMAT_PATTERN);
        LocalDate endDate = LocalDate.parse(config.getValidEndDate(), DATE_FORMAT_PATTERN);
        bo.setBuilder(PeriodOfValidityBO.custom(Date.from(
                startDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                , Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));
    }

    /**
     * 2023-03-22 18:12
     * 永久有效
     *
     * @param preUserByTask 原始数据集合
     * @return Map key：custNum value：Map key：userType value：SyncUserValidityPeriodBO {@linkplain SyncUserValidityPeriodBO MarketingSyncUser PeriodOfValidityBO.Builder}
     */
    private Map<String, Map<String, SyncUserValidityPeriodBO>> longValidUserType(List<MarketingSyncUser> preUserByTask) {
        return preUserByTask.parallelStream().collect(Collectors.groupingBy(MarketingSyncUser::getCustNum)).entrySet()
                .parallelStream().collect(Collectors.toConcurrentMap(Map.Entry::getKey, stringListEntry ->
                        stringListEntry.getValue().parallelStream().collect(Collectors.toConcurrentMap(
                                MarketingSyncUser::getUserType, marketingSyncUser -> {
                                    SyncUserValidityPeriodBO bo = new SyncUserValidityPeriodBO();
                                    bo.setSyncUser(marketingSyncUser);
                                    bo.setBuilder(PeriodOfValidityBO.custom(marketingSyncUser.getAppletTime(), null));
                                    return bo;
                                }, this::latestSyncUserValidityPeriodBO))));
    }


    /**
     * shijian
     */
    @Override
    public MarketingSyncUser getMarketingSyncUserDidi(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        MarketingSyncUser marketingSyncUser = null;
        // 1. 查询配置表

        List<MarketingDataValidConfig> marketingDataValidConfigs = marketingDataValidConfigMapper.selectInfo(marketingTransferSyncUser.getApiCode(), marketingTransferSyncUser.getUserType());
        // 获取需要判断的指定日期
        requestDate = requestDate == null ? marketingTransferSyncUser.getRequestData() : requestDate;
        LocalDate parse = LocalDate.parse(requestDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));

        // 2. 获取T,N 模式下 有效期范围的规则集合，T，N
        List<MarketingDataValidConfig> marketingDataValidConfigTN = marketingDataValidConfigs.stream().filter(m -> m.getValidType() == 1).collect(Collectors.toList());
        List<MarketingDataValidConfig> collectRequestDateTN = new ArrayList<>();
        marketingDataValidConfigTN.forEach(mctn -> {
            String validStartDate = mctn.getValidStartDate();
            String validEndDate = mctn.getValidEndDate();
            LocalDate startDate = LocalDate.parse(validStartDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            LocalDate endDate = LocalDate.parse(validEndDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            if ((startDate.isBefore(parse) || startDate.isEqual(parse))
                    && (parse.isBefore(endDate) || parse.isEqual(endDate))) {
                collectRequestDateTN.add(mctn);
            }
        });
        // 如果T,N 模式不为空则查询最新一条数据
        if (collectRequestDateTN.size() > 0) {
            marketingSyncUser = marketingSyncUserMap.selectInAppletDate(collectRequestDateTN, marketingTransferSyncUser);
        }
        return marketingSyncUser;
    }

    /**
     * 新版有效期
     */
    private MarketingSyncUser getMarketingSyncUserFistVersion(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        MarketingSyncUser marketingSyncUser = null;
        // 1. 查询配置表

        List<MarketingDataValidConfig> marketingDataValidConfigs = marketingDataValidConfigMapper.selectInfoFirstVersion(marketingTransferSyncUser.getApiCode(), marketingTransferSyncUser.getUserType());
        // 获取需要判断的指定日期
        requestDate = requestDate == null ? marketingTransferSyncUser.getRequestData() : requestDate;
        LocalDate parse = LocalDate.parse(requestDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));

        // 2. 获取T,N 模式下 有效期范围的规则集合，T，N
        List<MarketingDataValidConfig> collectRequestDateTN = new ArrayList<>();
        marketingDataValidConfigs.forEach(mctn -> {
            String validStartDate = mctn.getValidStartDate();
            String validEndDate = mctn.getValidEndDate();
            LocalDate startDate = LocalDate.parse(validStartDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            LocalDate endDate = LocalDate.parse(validEndDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            if ((startDate.isBefore(parse) || startDate.isEqual(parse))
                    && (parse.isBefore(endDate) || parse.isEqual(endDate))) {
                collectRequestDateTN.add(mctn);
            }
        });

        // 如果T,N 模式不为空则查询最新一条数据
        if (collectRequestDateTN.size() > 0) {

            marketingSyncUser = marketingSyncUserMap.selectInAppletDate(collectRequestDateTN, marketingTransferSyncUser);
        }

        return marketingSyncUser;
    }

    @Override
    public Map<String, SyncUserValidityPeriodBO> getValidityPeriodUserTypeBatchFirstVersion(
            List<MarketingTransferSyncUser> transferSyncUserList, final String apiCode, Object requestDateObj) {
        // apicode有效期配置
        final List<MarketingDataValidConfig> configList = getDataValidConfig(transferSyncUserList, apiCode);
        if (configList == null) {
            return Collections.emptyMap();
        }
        Map<String, SyncUserValidityPeriodBO> boMap = new ConcurrentHashMap<>(2048);
        // 获取转化数据的请求日期
        getTransferDataRequestDate(transferSyncUserList, requestDateObj).forEach((k, v) -> {
            // 配置了T,T （范围）模式的情况
            final Set<String> userTypeSet = v.stream().map(MarketingTransferSyncUser::getUserType)
                    .collect(Collectors.toSet());
            // 获取包含请求日期的T,T （范围）模式的配置记录
            List<MarketingDataValidConfig> dataValidConfigs = configList.stream()
                    .filter(config -> userTypeSet.contains(config.getUserType())
                            && compareRequestDate(config, k)).collect(Collectors.toList());
            userTypeExistDataValidConfigCheck(dataValidConfigs, userTypeSet, apiCode);
            // 包含请求日期的T,T （范围）模式的配置记录不为空则查询最新一条数据原始数据（上传数据）
            if (!dataValidConfigs.isEmpty()) {
                List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserLastByInAppletDateUserTypeList(
                        apiCode, dataValidConfigs, v);
                boMap.putAll(packageKeyValidityPeriodInfo(syncUser -> syncUser.getCustNum() + syncUser.getUserType()
                        , syncUserList, dataValidConfigs));
            }
        });
        return boMap;
    }

    @Override
    public Map<String, SyncUserValidityPeriodBO> getValidityPeriodCellBatchFirstVersion(
            List<CellValidityPeriodBO> cellValidityPeriodBOList, String apiCode, Object requestDateObj) {
        // apicode有效期配置
        final List<MarketingDataValidConfig> configList = getDataValidConfig(cellValidityPeriodBOList, apiCode);
        if (configList == null) {
            return Collections.emptyMap();
        }
        Map<String, SyncUserValidityPeriodBO> boMap = new ConcurrentHashMap<>(2048);
        // 获取转化数据的请求日期
        getCellValidityPeriodBORequestDate(cellValidityPeriodBOList, requestDateObj).forEach((k, v) -> {
            // 配置了T,T （范围）模式的情况
            final Set<String> userTypeSet = v.stream().map(CellValidityPeriodBO::getUserType).filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            boolean empty = userTypeSet.isEmpty();
            // 获取包含请求日期的T,T （范围）模式的配置记录
            final List<MarketingDataValidConfig> dataValidConfigs;
            if (empty) {
                dataValidConfigs = configList.stream().filter(config -> compareRequestDate(config, k))
                        .collect(Collectors.toList());
                // 包含请求日期的T,T （范围）模式的配置记录不为空则查询最新一条数据原始数据（上传数据）
                if (!dataValidConfigs.isEmpty()) {
                    List<MarketingSyncUser> syncUserList =
                            marketingSyncUserMapper.getSyncUserLastByCellAndInAppletDateUserTypeList(apiCode
                                    , dataValidConfigs, v);
                    boMap.putAll(packageKeyValidityPeriodInfo(MarketingSyncUser::getCell, syncUserList, dataValidConfigs));
                }
            } else {
                dataValidConfigs = configList.stream()
                        .filter(config -> userTypeSet.contains(config.getUserType())
                                && compareRequestDate(config, k)).collect(Collectors.toList());
                userTypeExistDataValidConfigCheck(dataValidConfigs, userTypeSet, apiCode);
                // 包含请求日期的T,T （范围）模式的配置记录不为空则查询最新一条数据原始数据（上传数据）
                if (!dataValidConfigs.isEmpty()) {
                    List<MarketingSyncUser> syncUserList =
                            marketingSyncUserMapper.getSyncUserLastByCellAndInAppletDateUserTypeList(
                                    apiCode, dataValidConfigs, v);
                    boMap.putAll(packageKeyValidityPeriodInfo(syncUser -> syncUser.getCell() + syncUser.getUserType()
                            , syncUserList, dataValidConfigs));
                }
            }
        });
        return boMap;
    }

    @Override
    public Map<String, SyncUserValidityPeriodBO> getValidityPeriodCellBatchFirstVersion(Set<String> cellSet
            , String apiCode, Object requestDateObj) {
        // apicode有效期配置
        final List<MarketingDataValidConfig> configList = getDataValidConfig(cellSet, apiCode);
        if (configList == null) {
            return Collections.emptyMap();
        }
        // 统一时间格式
        final Date requestDate = switchDate(requestDateObj);
        // 获取包含请求日期的T,T （范围）模式的配置记录
        final List<MarketingDataValidConfig> dataValidConfigs = configList.stream()
                .filter(config -> compareRequestDate(config, requestDate)).collect(Collectors.toList());
        Map<String, SyncUserValidityPeriodBO> boMap = new ConcurrentHashMap<>(2048);
        // 包含请求日期的T,T （范围）模式的配置记录不为空则查询最新一条数据原始数据（上传数据）
        if (!dataValidConfigs.isEmpty()) {
            List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserLastByCellAndInAppletDatList(
                    apiCode, dataValidConfigs, cellSet);
            boMap.putAll(packageKeyValidityPeriodInfo(MarketingSyncUser::getCell, syncUserList, dataValidConfigs));
        }
        return boMap;
    }

    @Override
    public Map<String, SyncUserValidityPeriodBO> getValidityPeriodCustNumBatchFirstVersion(Set<String> custNumSet
            , String apiCode, Object requestDateObj) {
        // apicode有效期配置
        final List<MarketingDataValidConfig> configList = getDataValidConfig(custNumSet, apiCode);
        if (configList == null) {
            return Collections.emptyMap();
        }
        Map<String, SyncUserValidityPeriodBO> boMap = new ConcurrentHashMap<>(2048);
        // 统一时间格式
        final Date requestDate = switchDate(requestDateObj);
        // 获取包含请求日期的T,T （范围）模式的配置记录
        final List<MarketingDataValidConfig> dataValidConfigs = configList.stream()
                .filter(config -> compareRequestDate(config, requestDate)).collect(Collectors.toList());
        // 包含请求日期的T,T （范围）模式的配置记录不为空则查询最新一条数据原始数据（上传数据）
        if (!dataValidConfigs.isEmpty()) {
            List<MarketingSyncUser> syncUserList =
                    marketingSyncUserMapper.getSyncUserLastByCustNumAndInAppletDatList(apiCode
                            , dataValidConfigs, custNumSet);
            boMap.putAll(packageKeyValidityPeriodInfo(MarketingSyncUser::getCustNum, syncUserList, dataValidConfigs));
        }
        return boMap;
    }


    /**
     * 2023-03-23 12:38
     * apicode全量有效期配置
     */
    private List<MarketingDataValidConfig> findConfigAllByApiCodeListFirstVersion(String apiCode) {
        MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(1).andValidTypeEqualTo(1);
        example.setOrderByClause("create_time desc, update_time desc");
        return marketingDataValidConfigMapper.selectByExample(example);
    }

    /**
     * 2023-07-13 17:31
     * 是否存在有效期配置
     *
     * @return true 不存在，false 存在
     */
    private boolean isNotExistDataValidConfig(List<MarketingDataValidConfig> configList, String apiCode) {
        // 未配置任何有效期
        if (CollectionUtils.isEmpty(configList)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_VALIDITY_PERIOD.getCode()
                    , apiCode + "未配置任何有效期，请配置对应的有效期规则"
                    , apiCode + AlarmSendCodeEnum.EXCEPTION_VALIDITY_PERIOD.getMessage()));
            return true;
        }
        return false;
    }

    /**
     * 2023-07-13 17:31
     * 场景是否存在有效期配置
     */
    private void userTypeExistDataValidConfigCheck(List<MarketingDataValidConfig> configList
            , Set<String> userTypeSet, String apiCode) {
        Set<String> configUserTypeSet = configList.stream().map(
                MarketingDataValidConfig::getUserType).collect(Collectors.toSet());
        Set<String> newSet = new HashSet<>(userTypeSet);
        newSet.removeAll(configUserTypeSet);
        // 未配置任何有效期
        if (newSet.size() > 0) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_VALIDITY_PERIOD.getCode()
                    , "场景未配置任何有效期，请配置对应的有效期规则;apiCode:" + apiCode + ";userType:" + newSet
                    , apiCode + "存在场景未配置有效期规则"));
        }
    }

    /**
     * 2023-07-14 13:31
     * 验证数据及获取合法的有效期配置
     */
    private List<MarketingDataValidConfig> getDataValidConfig(Collection<?> collection, String apiCode) {
        if (CollectionUtils.isEmpty(collection)) {
            return null;
        }
        // apicode有效期配置
        final List<MarketingDataValidConfig> configList = findConfigAllByApiCodeListFirstVersion(apiCode);
        // 未配置任何有效期
        if (isNotExistDataValidConfig(configList, apiCode)) {
            return null;
        }
        return configList;
    }

    /**
     * 2023-07-13 14:52
     * 自定义keyMapper组装有效期信息
     *
     * @param keyMapper    自定义key
     * @param syncUserList 上传数据集合
     * @param configList   有效期配置集合
     *                     <p>
     *                     key：keyMapper; value: SyncUserValidityPeriodBO
     */
    private Map<String, SyncUserValidityPeriodBO> packageKeyValidityPeriodInfo(
            Function<MarketingSyncUser, String> keyMapper,
            List<MarketingSyncUser> syncUserList
            , List<MarketingDataValidConfig> configList) {
        final Map<String, MarketingDataValidConfig> configMap = configList.stream().collect(Collectors.toMap(
                config -> config.getUserType() + config.getAppletDate()
                , Function.identity()
                , BinaryOperator.maxBy(Comparator.comparing(MarketingDataValidConfig::getCreateTime))));
        return syncUserList.stream().collect(Collectors.toConcurrentMap(
                keyMapper, user -> {
                    // 组装原始数据有效期
                    return packageMapValue(user, configMap);
                }, this::latestSyncUserValidityPeriodBO));
    }

    /**
     * 2023-03-22 18:19
     * 组装原始数据有效期
     */
    private SyncUserValidityPeriodBO packageMapValue(MarketingSyncUser user, final Map<String
            , MarketingDataValidConfig> validConfigMap) {
        // 组装原始数据有效期
        MarketingDataValidConfig validConfig = validConfigMap.get(user.getUserType() + user.getAppletDate());
        SyncUserValidityPeriodBO bo = new SyncUserValidityPeriodBO();
        bo.setSyncUser(user);
        if (validConfig == null) {
            return bo;
        }
        packageSyncUserValidityPeriodBO(bo, validConfig);
        return bo;
    }

    /**
     * 2023-04-07 9:52
     * 获取数据的请求日期
     *
     * @return key requestDate; value List<CellValidityPeriodBO>
     */
    private Map<Date, List<CellValidityPeriodBO>> getCellValidityPeriodBORequestDate(
            List<CellValidityPeriodBO> cellValidityPeriodBOList, Object requestDateObj) {
        Map<Date, List<CellValidityPeriodBO>> requestDateMap;
        if (requestDateObj == null) {
            // 对转化数据按请求日期分组
            requestDateMap = cellValidityPeriodBOList.parallelStream().collect(Collectors.groupingBy(c -> {
                try {
                    return DateUtils.parse(c.getRequestDate(), DateHelper.LINE_DATE_FORMAT);
                } catch (ParseException pe) {
                    // 日期格式解析失败时，使用当前时间
                    return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
                }
            }));
        } else {
            // 统一时间格式
            Date requestDate = switchDate(requestDateObj);
            requestDateMap = new HashMap<>(2);
            requestDateMap.put(requestDate, cellValidityPeriodBOList);
        }
        return requestDateMap;
    }

}
