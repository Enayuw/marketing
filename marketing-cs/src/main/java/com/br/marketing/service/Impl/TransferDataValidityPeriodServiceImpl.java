package com.br.marketing.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomizeDataValidConfigMapper;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

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

    private final MarketingCustomizeDataValidConfigMapper customizeDataValidConfigMapper;

    private final static DateTimeFormatter DATE_FORMAT_PATTERN = DateTimeFormatter.ofPattern(DATEFORMATPATTERN);


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
        return ObjectUtils.isEmpty(o)
            ? o1 : (ObjectUtils.isEmpty(o1)
            ? o : (ObjectUtils.isEmpty(o.getAppletTime())
            ? (ObjectUtils.isEmpty(o1.getAppletTime())
            ? (ObjectUtils.isEmpty(o.getCreateTime())
            ? (ObjectUtils.isEmpty(o1.getCreateTime())
            ? o : o1) : (ObjectUtils.isEmpty(o1.getCreateTime())
            ? o : (o.getCreateTime().compareTo(o1.getCreateTime()) > 0
            ? o : o1))) : o1) : (ObjectUtils.isEmpty(o1.getAppletTime())
            ? o : (o.getAppletTime().compareTo(o1.getAppletTime()) > 0
            ? o : o1))));
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


    /**
     * 2023-04-07 9:52
     * 获取数据的请求日期字符串，格式yyyy-MM-dd
     *
     * @param transferSyncUserList 转化数据
     * @param requestDateObj       任意格式的请求日期
     * @return key requestDateStr; value List<MarketingTransferSyncUser>
     */
    private Map<String, List<MarketingTransferSyncUser>> getTransferDataRequestDateStr(
        List<MarketingTransferSyncUser> transferSyncUserList, Object requestDateObj) {
        Map<String, List<MarketingTransferSyncUser>> requestDateMap;
        if (requestDateObj == null) {
            // 对转化数据按请求日期分组
            requestDateMap = transferSyncUserList.parallelStream().collect(Collectors.groupingBy(user -> {
                String requestData = user.getRequestData();
                if (StringUtils.isEmpty(requestData)) {
                    // 日期格式解析失败时，使用当前时间
                    try {
                        return LocalDateTime.parse(user.getRequestTime(), DateTimeFormatter.ofPattern(
                            DateHelper.LINE_DATE_COLON_TIME_FORMAT_SSS)).toLocalDate().toString();
                    } catch (Exception e) {
                        try {
                            return user.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                                .toLocalDate().toString();
                        } catch (Exception exception) {
                            return LocalDate.now().toString();
                        }
                    }
                }
                return requestData;
            }));
        } else {
            // 统一时间格式
            String requestDateStr = switchDateStr(requestDateObj);
            requestDateMap = new HashMap<>(2);
            requestDateMap.put(requestDateStr, transferSyncUserList);
        }
        return requestDateMap;
    }


    /**
     * 2023-07-28 16:36
     * 转换日期
     * 支持数据格式 String(yyyy-MM-dd)、Date、LocalDate、LocalDateTime、Long、Calendar
     *
     * @param requestDateObj 请求日期对象
     * @return 日期字符串，格式yyyy-MM-dd
     */
    private String switchDateStr(Object requestDateObj) {
        String requestDateStr;
        if (requestDateObj == null) {
            requestDateStr = LocalDate.now().toString();
        } else if (requestDateObj instanceof Date) {
            requestDateStr = ((Date) requestDateObj).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
        } else if (requestDateObj instanceof String) {
            requestDateStr = (String) requestDateObj;
        } else if (requestDateObj instanceof LocalDate) {
            requestDateStr = ((LocalDate) requestDateObj).toString();
        } else if (requestDateObj instanceof LocalDateTime) {
            requestDateStr = ((LocalDateTime) requestDateObj).atZone(ZoneId.systemDefault()).toLocalDate().toString();
        } else if (requestDateObj instanceof Long) {
            requestDateStr = new Date((Long) requestDateObj)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
        } else if (requestDateObj instanceof Calendar) {
            requestDateStr = ((Calendar) requestDateObj).getTime()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
        } else {
            throw new IllegalArgumentException("非法的参数：" + requestDateObj
                + ",支持格式 String(yyyy-MM-dd)、Date、LocalDate、LocalDateTime、Long、Calendar");
        }
        return requestDateStr;
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



    @Override
    public Map<String, SyncUserValidityPeriodBO> getValidityPeriodUserTypeBatchFirstVersion(
        List<MarketingTransferSyncUser> transferSyncUserList, final String apiCode, Object requestDateObj) {
        if (CollectionUtils.isEmpty(transferSyncUserList)) {
            return Collections.emptyMap();
        }
        Map<String, SyncUserValidityPeriodBO> boMap = new ConcurrentHashMap<>(2048);
        // 获取转化数据的请求日期
        getTransferDataRequestDateStr(transferSyncUserList, requestDateObj).forEach((k, v) -> {
            // 配置了T,T （范围）模式的情况
            final Set<String> userTypeSet = v.stream().map(MarketingTransferSyncUser::getUserType)
                .collect(Collectors.toSet());
            Set<String> newSet = new HashSet<>(userTypeSet);
            int page = 0;
            int pageSize = 2000;
            // 获取包含请求日期的T,T （范围）模式的配置记录
            for (; ; ) {
                List<MarketingDataValidConfig> dataValidConfigs = getDataValidConfig(
                    apiCode, k, userTypeSet, page, pageSize);
                boolean isLast = dataValidConfigs.size() < pageSize;
                userTypeExistDataValidConfigCheck(dataValidConfigs, newSet, apiCode, isLast);
                if (dataValidConfigs.isEmpty()) {
                    break;
                }
                // 包含请求日期的T,T （范围）模式的配置记录不为空则查询最新一条数据原始数据（上传数据）
                List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserLastByInAppletDateUserTypeList(
                    apiCode, dataValidConfigs, v);
                packageKeyValidityPeriodInfo(syncUser -> syncUser.getCustNum() + syncUser.getUserType()
                    , syncUserList, dataValidConfigs, boMap);
                if (isLast) {
                    break;
                }
                ++page;
            }
        });
        return boMap;
    }


    @Override
    public Map<String, SyncUserValidityPeriodBO> getValidityPeriodCellBatchFirstVersion(Set<String> cellSet
        , String apiCode, Object requestDateObj) {
        if (CollectionUtils.isEmpty(cellSet)) {
            return Collections.emptyMap();
        }
        // 统一时间格式
        final String requestDate = switchDateStr(requestDateObj);
        // 获取包含请求日期的T,T （范围）模式的配置记录
        List<MarketingDataValidConfig> dataValidConfigs;
        Map<String, SyncUserValidityPeriodBO> boMap = new ConcurrentHashMap<>(2048);
        int page = 0;
        int pageSize = 2000;
        for (; ; ) {
            dataValidConfigs = getDataValidConfig(apiCode, requestDate, null, page, pageSize);
            if (dataValidConfigs.isEmpty()) {
                break;
            }
            // 包含请求日期的T,T （范围）模式的配置记录不为空则查询最新一条数据原始数据（上传数据）
            List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserLastByCellAndInAppletDatList(
                apiCode, dataValidConfigs, cellSet);
            packageKeyValidityPeriodInfo(MarketingSyncUser::getCell, syncUserList, dataValidConfigs, boMap);
            if (dataValidConfigs.size() < pageSize) {
                break;
            }
            ++page;
        }
        return boMap;
    }

    @Override
    public Map<String, SyncUserValidityPeriodBO> getValidityPeriodCustNumBatchFirstVersion(Set<String> custNumSet
        , String apiCode, Object requestDateObj) {
        if (CollectionUtils.isEmpty(custNumSet)) {
            return Collections.emptyMap();
        }
        Map<String, SyncUserValidityPeriodBO> boMap = new ConcurrentHashMap<>(2048);
        // 统一时间格式
        final String requestDateStr = switchDateStr(requestDateObj);
        // 获取包含请求日期的T,T （范围）模式的配置记录
        List<MarketingDataValidConfig> dataValidConfigs;
        int page = 0;
        int pageSize = 2000;
        for (; ; ) {
            dataValidConfigs = getDataValidConfig(apiCode, requestDateStr, null, page, pageSize);
            if (dataValidConfigs.isEmpty()) {
                break;
            }
            // 包含请求日期的T,T （范围）模式的配置记录不为空则查询最新一条数据原始数据（上传数据）
            List<MarketingSyncUser> syncUserList =
                marketingSyncUserMapper.getSyncUserLastByCustNumAndInAppletDatList(apiCode
                    , dataValidConfigs, custNumSet);
            packageKeyValidityPeriodInfo(MarketingSyncUser::getCustNum, syncUserList, dataValidConfigs, boMap);
            if (dataValidConfigs.size() < pageSize) {
                break;
            }
            ++page;
        }
        return boMap;
    }

    /**
     * 根据custNum获取多组有效期期范围 Tips：仅支持新版有效期规则，有效期配置valid_start_date和valid_end_date字段都非空
     *
     * @param custNumSet     custNum集合
     * @param apiCode        apiCode
     * @param requestDateObj 日期
     * @return {@link Map }<{@link String }, {@link SyncUserValidityPeriodsBO }>
     * @author senyang.zheng
     * @date 2023/10/07
     */
    @Override
    public Map<String, SyncUserValidityPeriodsBO> getValidityPeriodsByCustNum(Set<String> custNumSet, String apiCode, Object requestDateObj) {
        if (CollectionUtils.isEmpty(custNumSet)) {
            return Collections.emptyMap();
        }
        Map<String, SyncUserValidityPeriodsBO> resultMap = new ConcurrentHashMap<>(2048);
        //统一时间格式
        final String requestDateStr = switchDateStr(requestDateObj);
        //获取有效期配置不分页
        List<MarketingDataValidConfig> configList = getDataValidConfig(apiCode, requestDateStr, null, null, null);
        if (CollectionUtil.isEmpty(configList)) {
            return resultMap;
        }
        //包含请求日期的T,T （范围）模式的配置记录不为空则查询所有符合的上传数据
        List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserByCustNumAndAppletDateList(apiCode, configList, custNumSet);
        //组装有效期数据
        buildValidityPeriodsInfo(syncUserList, configList, resultMap);
        return resultMap;
    }

    /**
     * 构建有效期信息
     *
     * @param syncUserList 有效上传数据集合
     * @param configList   有效期配置集合
     * @param resultMap    返回结果集
     * @author senyang.zheng
     * @date 2023/10/08
     */
    private void buildValidityPeriodsInfo(List<MarketingSyncUser> syncUserList, List<MarketingDataValidConfig> configList,
                                          Map<String, SyncUserValidityPeriodsBO> resultMap) {
        Map<String, List<MarketingSyncUser>> custNumMap = syncUserList.stream().collect(Collectors.groupingBy(MarketingSyncUser::getCustNum));
        Map<String, MarketingDataValidConfig> configMap =
            configList.stream().collect(Collectors.toMap(config -> config.getUserType() + config.getAppletDate(), Function.identity(),
                BinaryOperator.maxBy(Comparator.comparing(c -> c.getUpdateTime() == null ? c.getCreateTime() : c.getUpdateTime()))));
        custNumMap.forEach((key, value) -> resultMap.put(key, buildSyncUserValidityPeriodsBO(value, configMap)));
    }

    /**
     * @param syncUsers 上传数据集合
     * @param configMap 根据 userType + appletDate 将有效期集合分组的结果集
     * @return {@link SyncUserValidityPeriodsBO }
     * @author senyang.zheng
     * @date 2023/10/08
     */
    private SyncUserValidityPeriodsBO buildSyncUserValidityPeriodsBO(List<MarketingSyncUser> syncUsers,
                                                                     Map<String, MarketingDataValidConfig> configMap) {
        SyncUserValidityPeriodsBO validityPeriodsBO = new SyncUserValidityPeriodsBO();
        syncUsers.forEach( (MarketingSyncUser syncUser) -> {
            String configKey = syncUser.getUserType() + syncUser.getAppletDate();
            MarketingDataValidConfig config = configMap.get(configKey);
            if (config != null) {
                PeriodOfValidityBO.Builder builder = PeriodOfValidityBO.custom(
                        Date.from(LocalDate.parse(config.getValidStartDate(), DATE_FORMAT_PATTERN).atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        Date.from(LocalDate.parse(config.getValidEndDate(), DATE_FORMAT_PATTERN).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                validityPeriodsBO.getSyncUsers().add(syncUser);
                validityPeriodsBO.getBuilders().add(builder);
            }
        });
        //倒序排序
        validityPeriodsBO.getBuilders().sort(Comparator.comparing(b -> b.builder().getEnDate(), Comparator.reverseOrder()));
        validityPeriodsBO.getSyncUsers().sort(Comparator.comparing(MarketingSyncUser::getAppletTime, Comparator.reverseOrder()));
        return validityPeriodsBO;
    }

    /**
     * 2024-08-28 23:14
     * 场景有效映射，组装有效期
     *
     * @param syncUsers 上传数据
     * @param configMap 有效期配置
     * @return map key：userType, value: {@link SyncUserValidityPeriodsBO}
     */
    private Map<String, SyncUserValidityPeriodsBO> buildSyncUserValidityPeriodUserTypeBO(
            List<MarketingSyncUser> syncUsers, Map<String, MarketingDataValidConfig> configMap) {
        Map<String, SyncUserValidityPeriodsBO> map = new HashMap<>();
        // 按场景分组
        Map<String, List<MarketingSyncUser>> collect = syncUsers.stream().collect(Collectors.groupingBy(
                MarketingSyncUser::getUserType));
        collect.forEach((String userType, List<MarketingSyncUser> syncUserList) -> {
            SyncUserValidityPeriodsBO validityPeriodsBO = new SyncUserValidityPeriodsBO();
            syncUserList.forEach((MarketingSyncUser syncUser) -> {
                String configKey = syncUser.getUserType() + syncUser.getAppletDate();
                MarketingDataValidConfig config = configMap.get(configKey);
                if (config != null) {
                    PeriodOfValidityBO.Builder builder = PeriodOfValidityBO.custom(
                            Date.from(LocalDate.parse(config.getValidStartDate(), DATE_FORMAT_PATTERN).atStartOfDay(
                                    ZoneId.systemDefault()).toInstant()),
                            Date.from(LocalDate.parse(config.getValidEndDate(), DATE_FORMAT_PATTERN).atStartOfDay(
                                    ZoneId.systemDefault()).toInstant()));
                    builder.addBeginDateStrAndEnDateStr(config.getValidStartDate(), config.getValidEndDate());
                    validityPeriodsBO.getSyncUsers().add(syncUser);
                    validityPeriodsBO.getBuilders().add(builder);
                }
            });
            //倒序排序
            validityPeriodsBO.getBuilders().sort(Comparator.comparing(b -> b.builder().getEnDate(), Comparator.reverseOrder()));
            validityPeriodsBO.getSyncUsers().sort(Comparator.comparing(MarketingSyncUser::getAppletTime, Comparator.reverseOrder()));
            // 按场景生成场景有效期映射
            map.put(userType, validityPeriodsBO);
        });
        return map;
    }

    /**
     * 根据custNum+userType获取多组有效期范围 Tips：仅支持新版有效期规则，有效期配置valid_start_date和valid_end_date字段都非空
     *
     * @param custNumSet     custNum集合
     * @param userType       场景
     * @param apiCode        apiCode
     * @param requestDateObj 日期
     * @return {@link Map }<{@link String }, {@link SyncUserValidityPeriodsBO }>
     * @author senyang.zheng
     * @date 2023/12/08
     */
    @Override
    public Map<String, SyncUserValidityPeriodsBO> getValidityPeriodsByCustNumAndUserType(Set<String> custNumSet,
                                                                                         String userType,
                                                                                         String apiCode,
                                                                                         Object requestDateObj) {
        if (CollectionUtils.isEmpty(custNumSet) || StringUtils.isEmpty(userType) || StringUtils.isEmpty(apiCode)) {
            return Collections.emptyMap();
        }
        Map<String, SyncUserValidityPeriodsBO> resultMap = new ConcurrentHashMap<>(2048);
        //统一时间格式
        final String requestDateStr = switchDateStr(requestDateObj);
        //获取有效期配置不分页
        List<MarketingDataValidConfig> configList = getDataValidConfig(apiCode, requestDateStr, Sets.newHashSet(userType), null, null);
        if (CollectionUtil.isEmpty(configList)) {
            return resultMap;
        }
        //包含请求日期的T,T （范围）模式的配置记录不为空则查询所有符合的上传数据
        List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserByCustNumAndAppletDateList(apiCode, configList, custNumSet);
        //根据自定义Key组装有效期数据
        buildValidityPeriodsInfoByKeyMapper(MarketingSyncUser::getCustNum,syncUserList, configList, resultMap);
        return resultMap;
    }

    /**
     * 根据上传数据cell+userType获取多组有效期范围 Tips：仅支持新版有效期规则，有效期配置valid_start_date和valid_end_date字段都非空
     *
     * @param cellSet        cell集合
     * @param userType       场景
     * @param apiCode        apiCode
     * @param requestDateObj 日期
     * @return {@link Map }<{@link String }, {@link SyncUserValidityPeriodsBO }>
     * @author senyang.zheng
     * @date 2023/12/08
     */
    @Override
    public Map<String, SyncUserValidityPeriodsBO> getValidityPeriodsByCellAndUserType(Set<String> cellSet,
                                                                                      String userType,
                                                                                      String apiCode,
                                                                                      Object requestDateObj) {
        if (CollectionUtils.isEmpty(cellSet) || StringUtils.isEmpty(userType) || StringUtils.isEmpty(apiCode)) {
            return Collections.emptyMap();
        }
        Map<String, SyncUserValidityPeriodsBO> resultMap = new ConcurrentHashMap<>(2048);
        //统一时间格式
        final String requestDateStr = switchDateStr(requestDateObj);
        //获取有效期配置不分页  apiCode + bizDate + userType
        List<MarketingDataValidConfig> configList = getDataValidConfig(apiCode, requestDateStr, Sets.newHashSet(userType), null, null);
        if (CollectionUtil.isEmpty(configList)) {
            return resultMap;
        }
        //包含请求日期的T,T （范围）模式的配置记录不为空则查询所有符合的上传数据
        // apiCode + cellSet + (applet_date, user_type)
        List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserByCellAndAppletDateList(apiCode, configList, cellSet);
        //根据自定义Key组装有效期数据
        buildValidityPeriodsInfoByKeyMapper(MarketingSyncUser::getCell, syncUserList, configList, resultMap);
        return resultMap;
    }

    /**
     * 根据上传数据cell获取多组有效期范围 Tips：仅支持新版有效期规则，有效期配置valid_start_date和valid_end_date字段都非空
     *
     * @param cellSet        cell集合
     * @param apiCode        apiCode
     * @param requestDateObj 日期
     * @return {@link Map }<{@link String }, {@link SyncUserValidityPeriodsBO }>
     * @author senyang.zheng
     * @date 2023/12/08
     */
    @Override
    public Map<String, SyncUserValidityPeriodsBO> getValidityPeriodsByCells(Set<String> cellSet, String apiCode, Object requestDateObj) {
        if (CollectionUtils.isEmpty(cellSet) || StringUtils.isEmpty(apiCode)) {
            return Collections.emptyMap();
        }
        Map<String, SyncUserValidityPeriodsBO> resultMap = new ConcurrentHashMap<>(2048);
        // 统一时间格式
        final String requestDateStr = switchDateStr(requestDateObj);
        // 获取有效期配置不分页
        List<MarketingDataValidConfig> configList = getDataValidConfig(apiCode, requestDateStr, null, null, null);
        if (CollectionUtil.isEmpty(configList)) {
            return resultMap;
        }
        // 包含请求日期的T,T （范围）模式的配置记录不为空则查询所有符合的上传数据
        List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserByCellAndAppletDateList(apiCode, configList, cellSet);
        // 组装有效期数据
        buildValidityPeriodsInfo(syncUserList, configList, resultMap);
        return resultMap;
    }

    private void buildValidityPeriodsInfoByKeyMapper(Function<MarketingSyncUser, String> keyMapper,
                                                     List<MarketingSyncUser> syncUserList,
                                                     List<MarketingDataValidConfig> configList,
                                                     Map<String, SyncUserValidityPeriodsBO> resultMap) {
        // key: cell, value: List<MarketingSyncUser>>
        Map<String, List<MarketingSyncUser>> custNumMap = syncUserList.stream().collect(Collectors.groupingBy(keyMapper));
        // key: UserType + appletDate, value: MarketingDataValidConfig
        Map<String, MarketingDataValidConfig> configMap =
                configList.stream().collect(Collectors.toMap(config -> config.getUserType() + config.getAppletDate(), Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(c -> c.getUpdateTime() == null ? c.getCreateTime() : c.getUpdateTime()))));
        // key: cell, value: SyncUserValidityPeriodsBO
        custNumMap.forEach((key, value) -> resultMap.put(key, buildSyncUserValidityPeriodsBO(value, configMap)));
    }


    /**
     * 2024-08-28 23:12
     * 案件编号与场景有效期映射
     *
     * @param keyMapper    key生成函数
     * @param syncUserList 上传数据集合
     * @param configList   有效期配置集合
     * @param resultMap    场景有效期
     */
    private void buildValidityPeriodsUserTypeInfoByKeyMapper(Function<MarketingSyncUser, String> keyMapper,
                                                             List<MarketingSyncUser> syncUserList,
                                                             List<MarketingDataValidConfig> configList,
                                                             Map<String, Map<String, SyncUserValidityPeriodsBO>> resultMap) {
        // key: UserType + appletDate, value: MarketingDataValidConfig
        Map<String, MarketingDataValidConfig> configMap =
                configList.stream().collect(Collectors.toMap(config -> config.getUserType() + config.getAppletDate()
                        , Function.identity(), BinaryOperator.maxBy(Comparator.comparing(
                                c -> c.getUpdateTime() == null ? c.getCreateTime() : c.getUpdateTime()))));
        // key: custNum, value: List<MarketingSyncUser>>
        Map<String, List<MarketingSyncUser>> custNumMap = syncUserList.stream().collect(Collectors.groupingBy(keyMapper));
        // key: custNum, value: Map<String, SyncUserValidityPeriodsBO>  key: userType, value:SyncUserValidityPeriodsBO
        custNumMap.forEach((key, value) -> resultMap.put(key, buildSyncUserValidityPeriodUserTypeBO(value, configMap)));
    }

    @Override
    public List<MarketingDataValidConfig> getDataValidityPeriodPageList(
            String apiCode, Object requestDateObj, Integer page, Integer pageSize) {
        //统一时间格式
        String requestDateStr = switchDateStr(requestDateObj);
        return getDataValidConfig(apiCode, requestDateStr, null, page, pageSize);
    }

    /**
     * 根据上传数据cell+userType获取多组有效期范围 Tips：定制化有效期配置使用，有效期配置valid_start_date和valid_end_date字段都非空
     *
     * @param custNumSet     custNum集合
     * @param apiCode        apiCode
     * @param requestDateObj 日期
     * @Param taskIds        批次号集合
     * @return {@link Map }<{@link String }, {@link SyncUserValidityPeriodsBO }>
     * @author senyang.zheng
     * @date 2024/01/15
     */
    @Override
    public Map<String, SyncUserValidityPeriodsBO> getValidityPeriodsByCustNumAndTaskId(Set<String> custNumSet,
                                                                                       String apiCode,
                                                                                       Object requestDateObj) {
        if (CollectionUtils.isEmpty(custNumSet) || StringUtils.isEmpty(apiCode)) {
            return Collections.emptyMap();
        }
        Map<String, SyncUserValidityPeriodsBO> resultMap = new ConcurrentHashMap<>(2048);
        //统一时间格式
        final String requestDateStr = switchDateStr(requestDateObj);
        //获取有效期配置不分页
        MarketingCustomizeDataValidConfigExample example = new MarketingCustomizeDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andValidStartDateLessThanOrEqualTo(requestDateStr)
            .andValidEndDateGreaterThanOrEqualTo(requestDateStr);
        List<MarketingCustomizeDataValidConfig> configList = customizeDataValidConfigMapper.selectByExample(example);

        if (CollectionUtil.isEmpty(configList)) {
            return resultMap;
        }
        //包含请求日期的T,T （范围）模式的配置记录不为空则查询所有符合的上传数据
        List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserByCustNumAndTaskIdsList(apiCode, configList, custNumSet);
        //根据自定义Key组装有效期数据
        buildCustomizeValidityPeriodsInfoByKeyMapper(MarketingSyncUser::getCustNum, syncUserList, configList, resultMap);
        return resultMap;
    }

    private void buildCustomizeValidityPeriodsInfoByKeyMapper(Function<MarketingSyncUser, String> keyMapper,
                                                              List<MarketingSyncUser> syncUserList,
                                                              List<MarketingCustomizeDataValidConfig> configList,
                                                              Map<String, SyncUserValidityPeriodsBO> resultMap) {
        Map<String, List<MarketingSyncUser>> custNumMap = syncUserList.stream().collect(Collectors.groupingBy(keyMapper));
        Map<String, MarketingCustomizeDataValidConfig> configMap = configList.stream()
                .collect(Collectors.toMap(config -> config.getUserType() + config.getAppletDate() + config.getTaskId(), Function.identity(),
                    BinaryOperator.maxBy(Comparator.comparing(c -> c.getUpdateTime() == null ? c.getCreateTime() : c.getUpdateTime()))));
        custNumMap.forEach((key, value) -> resultMap.put(key, buildSyncUserCustomizeValidityPeriodsBO(value, configMap)));
    }

    private SyncUserValidityPeriodsBO buildSyncUserCustomizeValidityPeriodsBO(List<MarketingSyncUser> syncUsers,
        Map<String, MarketingCustomizeDataValidConfig> configMap) {
        SyncUserValidityPeriodsBO validityPeriodsBO = new SyncUserValidityPeriodsBO();
        syncUsers.forEach(syncUser -> {
            String configKey = syncUser.getUserType() + syncUser.getAppletDate() + syncUser.getCusBatch();
            MarketingCustomizeDataValidConfig config = configMap.get(configKey);
            if (config != null) {
                PeriodOfValidityBO.Builder builder = PeriodOfValidityBO.custom(
                    Date.from(LocalDate.parse(config.getValidStartDate(), DATE_FORMAT_PATTERN).atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    Date.from(LocalDate.parse(config.getValidEndDate(), DATE_FORMAT_PATTERN).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                validityPeriodsBO.getSyncUsers().add(syncUser);
                validityPeriodsBO.getBuilders().add(builder);
            }
        });
        // 倒序排序
        validityPeriodsBO.getBuilders().sort(Comparator.comparing(b -> b.builder().getEnDate(), Comparator.reverseOrder()));
        validityPeriodsBO.getSyncUsers().sort(Comparator.comparing(MarketingSyncUser::getAppletTime, Comparator.reverseOrder()));
        return validityPeriodsBO;
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
            MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
            example.createCriteria().andApiCodeEqualTo(apiCode).andValidTypeEqualTo(1).andIsDelEqualTo(1);
            int i = marketingDataValidConfigMapper.countByExample(example);
            if (i < 1) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode()
                        , "未配置任何有效期，请配置对应的有效期规则;apiCode:" + apiCode
                        , apiCode + AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getMessage()));
                return true;
            }
            log.warn("数据不在有效期范围;apiCode:" + apiCode);
            return true;
        }
        return false;
    }

    /**
     * 2023-08-01 17:31
     * 场景是否存在有效期配置
     */
    private void userTypeExistDataValidConfigCheck(List<MarketingDataValidConfig> configList
            , Set<String> userTypeSet, String apiCode, boolean isLast) {
        Set<String> configUserTypeSet = configList.stream().map(
                MarketingDataValidConfig::getUserType).collect(Collectors.toSet());
        userTypeSet.removeAll(configUserTypeSet);
        // 未配置任何有效期
        if (isLast && userTypeSet.size() > 0) {
            MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
            example.createCriteria().andApiCodeEqualTo(apiCode).andValidTypeEqualTo(1).andIsDelEqualTo(1)
                    .andUserTypeIn(new ArrayList<>(userTypeSet));
            int i = marketingDataValidConfigMapper.countByExample(example);
            if (i < 1) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode()
                        , "未配置任何有效期，请配置对应的有效期规则;apiCode:" + apiCode + ";userType:" + userTypeSet
                        , apiCode + AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getMessage()));
                return;
            }
            log.warn("数据不在有效期范围;apiCode:" + apiCode + ";userType:" + userTypeSet);
        }
    }

    @Override
    public Map<String, Map<String, SyncUserValidityPeriodsBO>> getValidityPeriodsByCustNumAndUserTypeSet(Set<String> custNumSet,
                                                                                                         Set<String> userTypeSet,
                                                                                                         String apiCode,
                                                                                                         Object requestDateObj) {
        if (CollectionUtils.isEmpty(custNumSet) || CollectionUtils.isEmpty(userTypeSet) || StringUtils.isEmpty(apiCode)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, SyncUserValidityPeriodsBO>> resultMap = new ConcurrentHashMap<>(2048);
        //统一时间格式
        final String requestDateStr = switchDateStr(requestDateObj);
        //获取有效期配置不分页
        List<MarketingDataValidConfig> configList = getDataValidConfig(apiCode, requestDateStr, userTypeSet
                , null, null);
        if (CollectionUtil.isEmpty(configList)) {
            return resultMap;
        }
        //包含请求日期的T,T （范围）模式的配置记录不为空则查询所有符合的上传数据
        List<MarketingSyncUser> syncUserList = marketingSyncUserMapper.getSyncUserByCustNumAndAppletDateList(
                apiCode, configList, custNumSet);
        //根据自定义Key组装有效期数据
        buildValidityPeriodsUserTypeInfoByKeyMapper(MarketingSyncUser::getCustNum, syncUserList, configList, resultMap);
        return resultMap;
    }


    /**
     * 2023-07-28 13:31
     * 验证数据及获取合法的有效期配置
     *
     * @param apiCode     编号
     * @param dateStr     日期
     * @param userTypeSet 场景集合
     * @return 有效期集合，未配置有效期时返回空集合
     */
    private List<MarketingDataValidConfig> getDataValidConfig(String apiCode, String dateStr, Set<String> userTypeSet
        , Integer page, Integer pageSize) {
        // apicode有效期配置
        List<MarketingDataValidConfig> configList = marketingDataValidConfigMapper
            .findListByApiCodeAndUserTypeSetPagetikv_(
                apiCode, dateStr, userTypeSet, page, pageSize);
        // 未配置任何有效期
        if (page != null && page == 0 && isNotExistDataValidConfig(configList, apiCode)) {
            return Collections.emptyList();
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
     */
    private void packageKeyValidityPeriodInfo(
        Function<MarketingSyncUser, String> keyMapper
        , List<MarketingSyncUser> syncUserList
        , List<MarketingDataValidConfig> configList
        , Map<String, SyncUserValidityPeriodBO> boMap) {
        final Map<String, MarketingDataValidConfig> configMap = configList.stream().collect(Collectors.toMap(
            config -> config.getUserType() + config.getAppletDate()
            , Function.identity()
            , BinaryOperator.maxBy(Comparator.comparing(c -> c.getUpdateTime() == null
                ? c.getCreateTime() : c.getUpdateTime()))));
        ConcurrentMap<String, SyncUserValidityPeriodBO> newBoMap = syncUserList.stream().collect(
            Collectors.toConcurrentMap(keyMapper, user -> {
                // 组装原始数据有效期
                return packageMapValue(user, configMap);
            }, this::latestSyncUserValidityPeriodBO));
        mergeSyncUserGetTimeLatest(boMap, newBoMap);
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
     * 2023-09-12 11:33
     * 合并map，获取时间最新的上传数据
     *
     * @param boMap    汇总集合，不可为null
     * @param newBoMap 分页集合
     */
    private void mergeSyncUserGetTimeLatest(final Map<String, SyncUserValidityPeriodBO> boMap
        , Map<String, SyncUserValidityPeriodBO> newBoMap) {
        if (CollectionUtils.isEmpty(newBoMap)) {
            return;
        }
        if (CollectionUtils.isEmpty(boMap)) {
            boMap.putAll(newBoMap);
            return;
        }
        newBoMap.forEach((k, v) -> boMap.merge(k, v, this::latestSyncUserValidityPeriodBO));
    }


    @Override
    public List<MarketingDataValidConfig> getDataValidityPeriodPageList(
            String apiCode, String userType, Object requestDateObj, int pageNo, int pageSize) {
        return getDataValidityPeriodPageList(apiCode, Collections.singleton(userType), requestDateObj, pageNo, pageSize);
    }

    @Override
    public List<MarketingDataValidConfig> getDataValidityPeriodPageList(
            String apiCode, Set<String> userTypeSet, Object requestDateObj, int pageNo, int pageSize) {
        //统一时间格式
        String requestDateStr = switchDateStr(requestDateObj);
        return getDataValidConfig(apiCode, requestDateStr, userTypeSet, pageNo, pageSize);
    }

    @Override
    public List<MarketingDataValidConfig> getDataMergeValidityPeriodList(String apiCode, String userType, Object requestDateObj) {
        int pageNo = 0;
        int pageSize = 2000;
        //统一时间格式
        String requestDateStr = switchDateStr(requestDateObj);
        Set<String> set = Collections.singleton(userType);
        List<MarketingDataValidConfig> mergeList = new ArrayList<>();
        for (; true; ) {
            List<MarketingDataValidConfig> list = getDataValidConfig(apiCode, requestDateStr, set, pageNo, pageSize);
            if (list.isEmpty()) {
                break;
            }
            // 收集分页的合并结果
            mergeList.addAll(mergeValidityPeriod(list));
            if (list.size() < pageSize) {
                break;
            }
            pageNo++;
        }
        return pageNo > 0 ? mergeValidityPeriod(mergeList) : mergeList;
    }

    /**
     * 2024-07-15 15:30
     * 合并时间段
     *
     * @param list 有效期配置集合
     * @return 合并时间段集合
     */
    private List<MarketingDataValidConfig> mergeValidityPeriod(List<MarketingDataValidConfig> list) {
        if (CollectionUtils.isEmpty(list) || list.size() == 1) {
            return list;
        }
        List<MarketingDataValidConfig> mergeList = new ArrayList<>();
        list.sort(Comparator.comparing(config -> LocalDate.parse(config.getValidStartDate())));
        for (MarketingDataValidConfig validConfig : list) {
            validConfig.setId(null);
            validConfig.setCreateTime(null);
            validConfig.setUpdateTime(null);
            validConfig.setAppletDate(null);
            LocalDate startDate = LocalDate.parse(validConfig.getValidStartDate());
            LocalDate mergeEndDate;
            MarketingDataValidConfig mergeValidConfig;
            if (mergeList.isEmpty()) {
                mergeList.add(validConfig);
            } else {
                mergeValidConfig = mergeList.get(mergeList.size() - 1);
                mergeEndDate = LocalDate.parse(mergeValidConfig.getValidEndDate());
                if (startDate.isAfter(mergeEndDate)) {
                    mergeList.add(validConfig);
                } else {
                    LocalDate endDate = LocalDate.parse(validConfig.getValidEndDate());
                    mergeValidConfig.setValidEndDate(mergeEndDate.isAfter(endDate)
                            ? mergeValidConfig.getValidEndDate() : validConfig.getValidEndDate());
                }
            }
        }
        return mergeList;
    }

}
