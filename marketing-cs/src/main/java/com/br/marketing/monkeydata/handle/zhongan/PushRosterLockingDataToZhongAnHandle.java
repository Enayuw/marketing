package com.br.marketing.monkeydata.handle.zhongan;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.bo.ZaMarketDataBO;
import com.br.marketing.bo.ZhonganRosterLockingDataBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.SftpFilePushSuccessDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.ZhonganMarketingBanMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.monkeydata.query.ZhongAnCellZkDateQuery;
import com.br.marketing.monkeydata.query.ZhongAnMobileMd5BizDateQuery;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.IMarketingDataValidService;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.util.PeriodOfValidityHelper;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 推送名单锁定数据到众安
 *
 * @author Guo Zeqiang
 * @dateTime 2022/11/14 11:43
 */
@Service
@Slf4j
public class PushRosterLockingDataToZhongAnHandle extends IMonkeyDataHandle<ZhonganRosterLockingData
        , ZhonganRosterLockingDataBO, Page2Condition<ZhonganRosterLockingData>> {
    @Resource
    private ZhonganRosterLockingDataMapper zhonganRosterLockingDataMapper;

    @Resource
    private IMarketingSyncUserService marketingSyncUserService;

    @Resource
    private CallRecordMapper callRecordMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private ZhonganMarketingBanMapper zhonganMarketingBanMapper;

    @Resource
    private IMarketingDataValidService iMarketingDataValidService;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    private static final String TITLE = "众安锁定名单推送告警";


    @Override
    public Result<IterationResult<ZhonganRosterLockingData, Page2Condition<ZhonganRosterLockingData>>> getInputData(
            Page2Condition<ZhonganRosterLockingData> condition) {
        Result<IterationResult<ZhonganRosterLockingData, Page2Condition<ZhonganRosterLockingData>>> result
                = new Result<>();
        try {
            // 以MobileMd5+BizDate分组
            List<ZhonganRosterLockingData> listPage = zhonganRosterLockingDataMapper
                    .findGroupMobileMd5ListPage(condition.getParam(), condition.getPageIndex(), condition.getPageSize());
            IterationResult<ZhonganRosterLockingData, Page2Condition<ZhonganRosterLockingData>> content
                    = new IterationResult<>();
            content.setInputDataList(listPage);
            content.setInDatacondition(condition);
            result.setDate(content);
            condition.setPageIndex(condition.getPageIndex());
            result.setCode(CollectionUtils.isEmpty(listPage)
                    ? ResultCode.FAIL.getValue() : ResultCode.SUCCESS.getValue());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }

    @Override
    public Result<?> customizedAction(Page2Condition<ZhonganRosterLockingData> condition) {
        Result<?> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(2, 2, 10);
        final ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(24, 24, new SynchronousQueue<>());
        ZhonganRosterLockingData lockingData = condition.getParam();
        Set<String> bizDates = zhonganRosterLockingDataMapper.getBizDateListtikv_(lockingData);
        List<Future<Result<List<ZhonganRosterLockingDataBO>>>> list = new ArrayList<>();
        int pageIndex = condition.getPageIndex();
        for (String bizDate : bizDates) {
            BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8)
                    , 250_0000, 0.01);
            ZhonganRosterLockingData param = condition.getParam();
            param.setBizDate(bizDate);
            param.setId(null);
            for (; ; ) {
                final List<ZhonganRosterLockingData> listPage = zhonganRosterLockingDataMapper.findPartColumnListPage(
                        param, pageIndex, condition.getPageSize());
                if (CollectionUtils.isEmpty(listPage)) {
                    break;
                }
                ZhonganRosterLockingData data = listPage.get(listPage.size() - 1);
                param.setId(data.getId());
                setThreadNumber(pool, condition.getParam().getTag(), pushPool);
                distinctMobile(listPage, bloomFilter);
                list.add(pool.submit(() -> processDataNew(listPage, pushPool)));
            }
        }
        for (Future<Result<List<ZhonganRosterLockingDataBO>>> future : list) {
            try {
                future.get(1, TimeUnit.MINUTES);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage()
                        , TITLE), e);
                result.setCode(ResultCode.FAIL.getValue());
            }
        }
        long taskCount = -1;
        pool.shutdown();
        try {
            while (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                long completedTask2Count = pool.getCompletedTaskCount();
                if (taskCount == completedTask2Count) {
                    result.setCode(ResultCode.FAIL.getValue());
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage()
                    , TITLE), e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        taskCount = -1;
        pushPool.shutdown();
        try {
            while (!pushPool.awaitTermination(10, TimeUnit.SECONDS)) {
                long completedTask2Count = pushPool.getCompletedTaskCount();
                if (taskCount == completedTask2Count) {
                    result.setCode(ResultCode.FAIL.getValue());
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage()
                    , TITLE), e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }

    private void distinctMobile(List<ZhonganRosterLockingData> listPage, BloomFilter<CharSequence> bloomFilter) {
        List<Long> ids = new ArrayList<>();
        // 组内去重
        Set<String> mobile = new HashSet<>(2000);
        final Iterator<ZhonganRosterLockingData> iterator = listPage.iterator();
        while (iterator.hasNext()) {
            ZhonganRosterLockingData next = iterator.next();
            if (!mobile.add(next.getMobileMd5())) {
                ids.add(next.getId());
                iterator.remove();
                continue;
            }
            if (bloomFilter.mightContain(next.getMobileMd5())) {
                ZhonganRosterLockingDataExample example = new ZhonganRosterLockingDataExample();
                example.createCriteria().andStatusEqualTo(1)
                        .andApiCodeEqualTo(next.getApiCode()).andBizDateEqualTo(next.getBizDate())
                        .andTagEqualTo(next.getTag()).andMobileMd5EqualTo(next.getMobileMd5());
                List<ZhonganRosterLockingData> list = zhonganRosterLockingDataMapper.getDuplicateMobileMd5List(
                        next.getApiCode(), next.getBizDate(), next.getTag(), next.getMobileMd5());
                int size = list.size();
                if (size == 1) {
                    if (list.get(0).getId().equals(next.getId())) {
                        continue;
                    }
                } else if (size > 1) {
                    List<ZhonganRosterLockingData> collect = list.parallelStream().filter(
                            l -> l.getId().equals(next.getId())).collect(Collectors.toList());
                    if (collect.size() > 0 || ids.contains(next.getId())) {
                        iterator.remove();
                        ids.add(next.getId());
                        continue;
                    }
                }
            }
            bloomFilter.put(next.getMobileMd5());
        }
        updatePushStatusById(ids, 6);
    }

    @Override
    public Result<List<ZhonganRosterLockingDataBO>> processData(List<ZhonganRosterLockingData> inList) {
        return null;
    }


    public Result<List<ZhonganRosterLockingDataBO>> processDataNew(List<ZhonganRosterLockingData> inList
            , ThreadPoolExecutor pushPool) {
        Result<List<ZhonganRosterLockingDataBO>> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());
        try {
            ZhonganRosterLockingData data = inList.get(0);
            String apiCode = data.getApiCode();
            String tag = data.getTag();
            Map<String, String> cellMap = md5ToLogMap(inList);
            Set<String> mobileMd5Set = new HashSet<>(cellMap.values());
            Map<String, SyncUserValidityPeriodBO> syncUserMap = transferDataValidityPeriodService
                    .getValidityPeriodCellBatchFirstVersion(mobileMd5Set, apiCode, data.getBizDate());
            boolean emptyBool = CollectionUtils.isEmpty(syncUserMap);
            if (emptyBool) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_VALIDITY_PERIOD.getCode()
                        , "tag:" + tag + ",apiCode:" + apiCode + "未获取到上传数据或未配置有效期！"
                        , apiCode + "," + tag + "锁定名单推送众安异常"));
                List<Long> ids = inList.parallelStream().map(ZhonganRosterLockingData::getId).collect(Collectors.toList());
                // 未获取到上传数据
                updatePushStatusById(ids, 4);
                return result;
            }
            HashMap<String, JSONObject> zhongAnDetailPush = marketingCommonConfig.getZhongAnDetailPush();
            //失效集合
            List<Long> notValidity = new ArrayList<>();
            Map<String, MarketingSyncUser> syncUserMapNew = inList.stream().filter(l -> {
                if (syncUserMap.containsKey(cellMap.get(l.getMobileMd5()))) {
                    return true;
                }
                notValidity.add(l.getId());
                return false;
            }).collect(Collectors.toConcurrentMap(d -> d.getMobileMd5() + d.getBizDate()
                    , l -> syncUserMap.get(cellMap.get(l.getMobileMd5())).getSyncUser()));
            Iterator<ZhonganRosterLockingData> iterator = inList.iterator();
            List<ZhonganRosterLockingDataBO> list = new ArrayList<>();
            List<Long> notPushData = new ArrayList<>();
            MarketingSyncUser syncUser;
            switch (tag) {
                case "CG":
                    // 对照组
                    Set<String> cellZkDateMap = getMarketingBanMap(apiCode, inList, cellMap);
                    List<Long> notMarketingList = new ArrayList<>();
                    while (iterator.hasNext()) {
                        ZhonganRosterLockingData next = iterator.next();
                        if ((syncUser = isPush(syncUserMapNew, zhongAnDetailPush, next, notPushData)) != null) {
                            String cell = cellMap.getOrDefault(next.getMobileMd5(), "");
                            if (cellZkDateMap.contains(cell + next.getBizDate())) {
                                // 不营销
                                notMarketingList.add(next.getId());
                                continue;
                            }
                            list.add(new ZhonganRosterLockingDataBO(next, syncUser, apiCode, tag));
                        }
                    }
                    updatePushStatusById(notMarketingList, 7);
                    break;
                case "MG":
                    // 营销组
                    Set<String> custNumBlackListSet = mgFilterCgPushNew(inList, syncUserMapNew, apiCode, syncUserMap);
                    iterator = inList.iterator();
                    List<Long> hitBlackList = new ArrayList<>();
                    while (iterator.hasNext()) {
                        ZhonganRosterLockingData next = iterator.next();
                        if ((syncUser = isPush(syncUserMapNew, zhongAnDetailPush, next, notPushData)) != null) {
                            // 判断黑名单
                            if (custNumBlackListSet.contains(syncUser.getCustNum() + next.getBizDate())) {
                                // 命中黑名单
                                hitBlackList.add(next.getId());
                                continue;
                            }
                            list.add(new ZhonganRosterLockingDataBO(next, syncUser, apiCode, tag));
                        }
                    }
                    updatePushStatusById(hitBlackList, 5);
                    break;
                default:
                    while (iterator.hasNext()) {
                        ZhonganRosterLockingData next = iterator.next();
                        String key = next.getMobileMd5() + next.getBizDate();
                        MarketingSyncUser user = syncUserMapNew.get(key);
                        if (ObjectUtils.isEmpty(user)) {
                            continue;
                        }
                        list.add(new ZhonganRosterLockingDataBO(next, user, apiCode, tag));
                    }
            }
            updatePushStatusById(notValidity, 4);
            updatePushStatusById(notPushData, 8);
            if (CollectionUtils.isEmpty(list)) {
                return result;
            }
            Result<?> resultAction = resultAction(list, pushPool);
            result.setCode(resultAction.getCode());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 2023-07-13 21:20
     * 更新数据状态 新版有效期
     */
    private MarketingSyncUser isPush(Map<String, MarketingSyncUser> syncUserMapNew
            , HashMap<String, JSONObject> pushConfig
            , ZhonganRosterLockingData next
            , List<Long> noPushData) {
        String mobileMd5 = next.getMobileMd5();
        String key = mobileMd5 + next.getBizDate();
        MarketingSyncUser syncUser = syncUserMapNew.get(key);
        // 不在有效期
        if (syncUser == null) {
            return null;
        }
        JSONObject push = pushConfig.get(syncUser.getUserType());
        // 未配置可推送
        if (push == null || !"1".equals(push.getString("isPush"))) {
            noPushData.add(next.getId());
            return null;
        }
        return syncUser;
    }

    /**
     * 2023-07-13 21:20
     * MG组过滤CG组已推送 新版有效期
     */
    private Set<String> mgFilterCgPushNew(List<ZhonganRosterLockingData> inList
            , Map<String, MarketingSyncUser> syncUserMapNew
            , String apiCode
            , final Map<String, SyncUserValidityPeriodBO> syncUserMap) {
        Map<String, String> custNumMap = new ConcurrentHashMap<>(1024);
        Map<String, PeriodOfValidityBO> custDayMap = new HashMap<>();
        Set<String> custNumBlackListSet = new HashSet<>();
        String nowDay = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ZhongAnMobileMd5BizDateQuery> queries = inList.stream()
                .filter(l -> syncUserMapNew.containsKey(l.getMobileMd5() + l.getBizDate()))
                .map(l -> {
                    MarketingSyncUser syncUser = syncUserMapNew.get(l.getMobileMd5() + l.getBizDate());
                    PeriodOfValidityBO bo = syncUserMap.get(syncUser.getCell())
                            .getBuilder().addOfDayTimeStrString().builder();
                    custNumMap.put(syncUser.getCustNum(), l.getBizDate());
                    custDayMap.put(l.getMobileMd5(), bo);
                    // 构造查询条件
                    return new ZhongAnMobileMd5BizDateQuery(l.getMobileMd5(), bo);
                }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(queries)) {
            return custNumBlackListSet;
        }

        List<ZhonganRosterLockingData> cgMobileMd5s = zhonganRosterLockingDataMapper.getMobileMd5ByBeforePush(queries
                , apiCode, "CG");
        if (!CollectionUtils.isEmpty(cgMobileMd5s)) {
            Set<String> cgMobileMd5Set = cgMobileMd5s.stream().filter(t -> {
                PeriodOfValidityBO periodOfValidityBO = custDayMap.get(t.getMobileMd5());
                if (periodOfValidityBO == null) {
                    return false;
                }
                try {
                    Date beginDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(periodOfValidityBO.getStartOfDayTimeStr());
                    Date endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(periodOfValidityBO.getEndOfDayTimeStr());
                    if (beginDate.compareTo(t.getCreateTime()) <= 0 && endDate.compareTo(t.getCreateTime()) >= 0) {
                        return true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return false;
            }).map(ZhonganRosterLockingData::getMobileMd5).collect(Collectors.toSet());

            // 过滤CG组是否已经推送过
            List<ZhonganRosterLockingData> list = inList.stream().filter(
                    l -> cgMobileMd5Set.contains(l.getMobileMd5())).collect(Collectors.toList());
            // 去掉CG组已推送
            inList.removeAll(list);
            List<Long> collect = list.parallelStream().map(ZhonganRosterLockingData::getId).collect(Collectors.toList());
            // 重复数据
            updatePushStatusById(collect, 6);
        }

        if (CollectionUtils.isEmpty(inList)) {
            return Collections.emptySet();
        }
        processTodayZhongaAnBlackData(custNumMap,custNumBlackListSet,nowDay);
        if (!CollectionUtils.isEmpty(custNumMap)) {
            List<CallRecord> blackListSettikv_ = callRecordMapper.getBlackListSettikv_(custNumMap, apiCode);
            if (!CollectionUtils.isEmpty(blackListSettikv_)) {
                custNumBlackListSet.addAll(blackListSettikv_.stream()
                        .map(t -> t.getCaseNum() + new SimpleDateFormat("yyyy-MM-dd").format(t.getCallStartTime()))
                        .collect(Collectors.toSet()));
            }
        }
        return custNumBlackListSet;
    }

    @Deprecated
    public Result<List<ZhonganRosterLockingDataBO>> processData(List<ZhonganRosterLockingData> inList
            , ThreadPoolExecutor pushPool) {
        Result<List<ZhonganRosterLockingDataBO>> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());
        ZhonganRosterLockingData data = inList.get(0);
        String apiCode = data.getApiCode();
        String tag = data.getTag();
        Map<String, String> cellMap = md5ToLogMap(inList);
        Set<String> mobileMd5Set = new HashSet<>(cellMap.values());
        Map<String, MarketingSyncUser> syncUserMap = marketingSyncUserService.getCellByCellAndMaxAppletTimeMap(apiCode
                , mobileMd5Set);
        boolean emptyBool = CollectionUtils.isEmpty(syncUserMap);
        if (emptyBool) {
            log.warn("tag:{},apiCode{},未获取到上传数据！", tag, apiCode);
            List<Long> ids = inList.parallelStream().map(ZhonganRosterLockingData::getId).collect(Collectors.toList());
            // 未获取到上传数据
            updatePushStatusById(ids, 3);
            return result;
        }
        HashMap<String, JSONObject> zhongAnDetailPush = marketingCommonConfig.getZhongAnDetailPush();
        Result<List<MarketingDataValidConfig>> dataValidConfigByType = iMarketingDataValidService.getDataValidConfigByType(apiCode, 3);
        AssertResult.assertResult(dataValidConfigByType);
        List<MarketingDataValidConfig> validConfigs = dataValidConfigByType.getData();
        Map<String, Integer> userTypeDays = validConfigs.stream().collect(Collectors.toMap(MarketingDataValidConfig::getUserType
                , t -> PeriodOfValidityHelper.getPeriodOfValidityDay(t.getValidDays())));
        Map<String, MarketingSyncUser> syncUserMapNew = inList.stream().filter(l -> syncUserMap.containsKey(
                cellMap.get(l.getMobileMd5()))).collect(Collectors.toConcurrentMap(d -> d.getMobileMd5() + d.getBizDate()
                , l -> syncUserMap.get(cellMap.get(l.getMobileMd5()))));
        Iterator<ZhonganRosterLockingData> iterator = inList.iterator();
        List<ZhonganRosterLockingDataBO> list = new ArrayList<>();
        //失效集合
        List<Long> notValidity = new ArrayList<>();
        //没有匹配上传数据集合
        List<Long> notUploadData = new ArrayList<>();
        //未配置有效期配置的数据
        List<Long> notValidConfigData = new ArrayList<>();
        //无需推送场景集合
        List<Long> notPushData = new ArrayList<>();
        MarketingSyncUser syncUser;
        switch (tag) {
            case "CG":
                // 对照组
                Set<String> cellZkDateMap = getMarketingBanMap(apiCode, inList, cellMap);
                List<Long> notMarketingList = new ArrayList<>();
                while (iterator.hasNext()) {
                    ZhonganRosterLockingData next = iterator.next();
                    if ((syncUser = isPush(syncUserMapNew, userTypeDays, zhongAnDetailPush, next, notValidity, notUploadData, notPushData, notValidConfigData)) != null) {
                        String cell = cellMap.getOrDefault(next.getMobileMd5(), "");
                        if (cellZkDateMap.contains(cell + next.getBizDate())) {
                            // 不营销
                            notMarketingList.add(next.getId());
                            continue;
                        }
                        list.add(new ZhonganRosterLockingDataBO(next, syncUser, apiCode, tag));
                    }
                }
                updatePushStatusById(notMarketingList, 7);
                break;
            case "MG":
                // 营销组
                Set<String> custNumBlackListSet = mgFilterCgPush(inList, syncUserMapNew, apiCode, userTypeDays);
                iterator = inList.iterator();
                List<Long> hitBlackList = new ArrayList<>();
                while (iterator.hasNext()) {
                    ZhonganRosterLockingData next = iterator.next();
                    if ((syncUser = isPush(syncUserMapNew, userTypeDays, zhongAnDetailPush, next, notValidity, notUploadData, notPushData, notValidConfigData)) != null) {
                        // 判断黑名单
                        if (custNumBlackListSet.contains(syncUser.getCustNum() + next.getBizDate())) {
                            // 命中黑名单
                            hitBlackList.add(next.getId());
                            continue;
                        }
                        list.add(new ZhonganRosterLockingDataBO(next, syncUser, apiCode, tag));
                    }
                }
                updatePushStatusById(hitBlackList, 5);
                break;
            default:
                while (iterator.hasNext()) {
                    ZhonganRosterLockingData next = iterator.next();
                    String key = next.getMobileMd5() + next.getBizDate();
                    MarketingSyncUser user = syncUserMapNew.get(key);
                    if (ObjectUtils.isEmpty(user)) {
                        notUploadData.add(next.getId());
                    } else {
                        list.add(new ZhonganRosterLockingDataBO(next, user, apiCode, tag));
                    }
                }
        }
        updatePushStatusById(notValidity, 4);
        updatePushStatusById(notUploadData, 3);
        updatePushStatusById(notPushData, 8);
        updatePushStatusById(notValidConfigData, 9);
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        Result<?> resultAction = resultAction(list, pushPool);
        result.setCode(resultAction.getCode());
        return result;
    }

    /**
     * 2022/11/19 13:40
     * 是否推送
     */
    @Deprecated
    private MarketingSyncUser isPush(Map<String, MarketingSyncUser> syncUserMapNew
            , Map<String, Integer> userTypeDay
            , HashMap<String, JSONObject> pushConfig
            , ZhonganRosterLockingData next
            , List<Long> notValidity
            , List<Long> notUploadData
            , List<Long> noPushData
            , List<Long> notValidConfigData) {
        String mobileMd5 = next.getMobileMd5();
        String key = mobileMd5 + next.getBizDate();
        // 未获取到上传数据
        if (!syncUserMapNew.containsKey(key)) {
            notUploadData.add(next.getId());
            return null;
        }
        MarketingSyncUser syncUser = syncUserMapNew.get(key);
        JSONObject push = pushConfig.get(syncUser.getUserType());
        // 未配置可推送
        if (push == null || !"1".equals(push.getString("isPush"))) {
            noPushData.add(next.getId());
            return null;
        }
        if (userTypeDay.get(syncUser.getUserType()) == null) {
            notValidConfigData.add(next.getId());
            return null;
        }
        Boolean validByThreeType = iMarketingDataValidService.isValidByThreeType(userTypeDay, syncUser);
        // 不在有效期内
        if (!validByThreeType) {
            notValidity.add(next.getId());
            return null;
        }
        return syncUser;
    }

    /**
     * 2022/11/19 13:41
     * MG组过滤CG组已推送
     */
    @Deprecated
    private Set<String> mgFilterCgPush(List<ZhonganRosterLockingData> inList
            , Map<String, MarketingSyncUser> syncUserMapNew
            , String apiCode
            , Map<String, Integer> userTypeDays) {
        Map<String, String> custNumMap = new ConcurrentHashMap<>(1024);
        Map<String, PeriodOfValidityBO> custDayMap = new HashMap<>();
        Set<String> custNumBlackListSet = new HashSet<>();
        String nowDay = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ZhongAnMobileMd5BizDateQuery> queries = inList.stream()
                .filter(l -> {
                    if (!syncUserMapNew.containsKey(l.getMobileMd5() + l.getBizDate())) {
                        return false;
                    }
                    MarketingSyncUser syncUser = syncUserMapNew.get(l.getMobileMd5() + l.getBizDate());
                    Integer integer = userTypeDays.get(syncUser.getUserType());
                    return integer != null;
                })
                .map(l -> {
                    MarketingSyncUser syncUser = syncUserMapNew.get(l.getMobileMd5() + l.getBizDate());
                    PeriodOfValidityBO periodOfValidityBO = marketingSyncUserService.getPeriodOfValidityRange(
                            userTypeDays.get(syncUser.getUserType())
                            , syncUser.getAppletTime() == null
                                    ? syncUser.getCreateTime()
                                    : syncUser.getAppletTime()).addOfDayTimeStrString().builder();
                    custNumMap.put(syncUser.getCustNum(), l.getBizDate());
                    custDayMap.put(l.getMobileMd5(), periodOfValidityBO);
                    return new ZhongAnMobileMd5BizDateQuery(l.getMobileMd5(), periodOfValidityBO);
                })
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(queries)) {
            return custNumBlackListSet;
        }

        List<ZhonganRosterLockingData> cgMobileMd5s = zhonganRosterLockingDataMapper.getMobileMd5ByBeforePush(queries, apiCode, "CG");
        if (!CollectionUtils.isEmpty(cgMobileMd5s)) {
            Set<String> cgMobileMd5Set = cgMobileMd5s.stream().filter(t -> {
                PeriodOfValidityBO periodOfValidityBO = custDayMap.get(t.getMobileMd5());
                if (periodOfValidityBO == null) {
                    return false;
                }
                try {
                    Date beginDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(periodOfValidityBO.getStartOfDayTimeStr());
                    Date endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(periodOfValidityBO.getEndOfDayTimeStr());
                    if (beginDate.compareTo(t.getCreateTime()) <= 0 && endDate.compareTo(t.getCreateTime()) >= 0) {
                        return true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return false;
            }).map(ZhonganRosterLockingData::getMobileMd5).collect(Collectors.toSet());

            // 过滤CG组是否已经推送过
            List<ZhonganRosterLockingData> list = inList.stream().filter(
                    l -> cgMobileMd5Set.contains(l.getMobileMd5())).collect(Collectors.toList());
            // 去掉CG组已推送
            inList.removeAll(list);
            List<Long> collect = list.parallelStream().map(ZhonganRosterLockingData::getId).collect(Collectors.toList());
            // 重复数据
            updatePushStatusById(collect, 6);
        }

        if (CollectionUtils.isEmpty(inList)) {
            return Collections.emptySet();
        }
        processTodayZhongaAnBlackData(custNumMap,custNumBlackListSet,nowDay);
        if (!CollectionUtils.isEmpty(custNumMap)) {
            List<CallRecord> blackListSettikv_ = callRecordMapper.getBlackListSettikv_(custNumMap, apiCode);
            if (!CollectionUtils.isEmpty(blackListSettikv_)) {
                custNumBlackListSet.addAll(blackListSettikv_.stream()
                        .map(t -> t.getCaseNum() + new SimpleDateFormat("yyyy-MM-dd").format(t.getCallStartTime()))
                        .collect(Collectors.toSet()));
            }
        }
        return custNumBlackListSet;
    }

    /**
     * 2022-12-12 17:54
     * 获取撞库手机号
     */
    private Set<String> getMarketingBanMap(String apiCode, List<ZhonganRosterLockingData> inList
            , Map<String, String> cellMap) {
        List<ZhongAnCellZkDateQuery> queries = inList.stream().map(l
                -> new ZhongAnCellZkDateQuery(cellMap.getOrDefault(l.getMobileMd5(), "")
                , l.getBizDate())).collect(Collectors.toList());
        return Optional.ofNullable(zhonganMarketingBanMapper.getNotMarketingCell(
                apiCode, queries)).orElse(new ArrayList<>()).parallelStream().map(
                b -> b.getCell() + b.getZkDate()).collect(Collectors.toSet());
    }

    /**
     * 2022/11/19 10:55
     * 更新数据状态
     */
    private void updatePushStatusById(List<Long> ids, int updateStatus) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ZhonganRosterLockingData data = new ZhonganRosterLockingData();
        data.setStatus(updateStatus);
        data.setUpdateTime(new Date());
        ZhonganRosterLockingDataExample example = new ZhonganRosterLockingDataExample();
        example.createCriteria().andIdIn(ids).andStatusEqualTo(1);
        zhonganRosterLockingDataMapper.updateByExampleSelective(data, example);
    }

    @Override
    public Result<?> resultAction(List<ZhonganRosterLockingDataBO> outputDataList) {
        return null;
    }

    public Result<?> resultAction(List<ZhonganRosterLockingDataBO> outputDataList, ThreadPoolExecutor pushPool) {
        Result<Object> result = new Result<>();
        if (CollectionUtils.isEmpty(outputDataList)) {
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }
        HashMap<String, JSONObject> zhongAnDetailPush = marketingCommonConfig.getZhongAnDetailPush();
        int size = outputDataList.size();
        int pushSize = 100;
        int count = 0;
        List<ZaMarketDetail> list = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (ZhonganRosterLockingDataBO bo : outputDataList) {
            ZaMarketDetail detail = new ZaMarketDetail();
            ZhonganRosterLockingData data = bo.getData();
            ids.add(data.getId());
            MarketingSyncUser syncUser = bo.getSyncUser();
            String channelCode = "MG".equals(data.getTag()) || "CG".equals(data.getTag())
                    ? zhongAnDetailPush.get(syncUser.getUserType()).getString("channelCode")
                    : ZhongAnClient.XdChannelCode;
            detail.setBizDate(data.getBizDate());
            detail.setTaskId(syncUser.getCusBatch());
            detail.setChannelCode(channelCode);
            detail.setTag(data.getTag());
            detail.setMobileMd5(data.getMobileMd5());
            list.add(detail);
            count++;
            if (list.size() == pushSize || size == count) {
                List<ZaMarketDetail> finalList = list;
                List<Long> finalDataList = ids;
                pushPool.execute(() -> {
                    ZaMarketDataDTO dataDTO = new ZaMarketDataDTO();
                    dataDTO.setData(finalList);
                    methodRetryHandlerService.callZhongAnData(new ZaMarketDataBO(dataDTO
                            , bo.getApiCode(), bo.getTag(), finalDataList), null);
                });
                list = new ArrayList<>();
                ids = new ArrayList<>();
            }
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    /**
     * 2022/11/22 17:28
     * 手机号md5转log加密
     * <p>
     * key MobileMd5
     * value cell log
     */
    private Map<String, String> md5ToLogMap(List<ZhonganRosterLockingData> inList) {
        return inList.parallelStream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ZhonganRosterLockingData::getMobileMd5)))
                , ArrayList::new)).stream().collect(Collectors.toConcurrentMap(ZhonganRosterLockingData::getMobileMd5, d -> {
            String query = RpcClientProxy.decode(d.getMobileMd5(), "cell", "md5", "");
            return StringUtils.isBlank(query) ? d.getMobileMd5() : BrCipherMaker.getInstance().encode(query);
        }, (v1, v2) -> v1));
    }

    /**
     * 2022/11/22 17:40
     * 配置线程
     */
    private void setThreadNumber(ThreadPoolExecutor pool, String tag, ThreadPoolExecutor pushPool) {
        Map<String, List<Integer>> zhongAnPushTreadPoolMap = marketingCommonConfig.getZhongAnPushTreadPoolSizeMap();
        List<Integer> zhongAnPushTreadPoolSize = zhongAnPushTreadPoolMap.get(tag);
        if (zhongAnPushTreadPoolSize == null) {
            zhongAnPushTreadPoolSize = zhongAnPushTreadPoolMap.get("other");
        }
        int size = zhongAnPushTreadPoolSize == null ? 0 : zhongAnPushTreadPoolSize.size();
        int poolSize;
        int pushPoolSize;
        if (size == 1) {
            Integer poolSizeNew = zhongAnPushTreadPoolSize.get(0);
            if (ObjectUtils.isEmpty(poolSizeNew) || poolSizeNew < 1) {
                return;
            }
            poolSize = poolSizeNew;
            pushPoolSize = poolSizeNew;
        } else if (size > 1) {
            Integer poolSizeNew = zhongAnPushTreadPoolSize.get(0);
            Integer pushPoolSizeNew = zhongAnPushTreadPoolSize.get(1);
            if (ObjectUtils.isEmpty(poolSizeNew) || poolSizeNew < 1) {
                poolSizeNew = Runtime.getRuntime().availableProcessors() * 10;
            }
            if (ObjectUtils.isEmpty(pushPoolSizeNew) || pushPoolSizeNew < 1) {
                pushPoolSizeNew = Runtime.getRuntime().availableProcessors() * 10;
            }
            poolSize = poolSizeNew;
            pushPoolSize = pushPoolSizeNew;
        } else {
            return;
        }
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, poolSize);
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pushPool, pushPoolSize);
    }

    /**
     * 2022/11/23 17:10
     * sftp 数据量统计
     */
    public void localFilePushStatis(String apiCode, String bizDate) {
        List<SftpFilePushSuccessDTO> successSum = zhonganRosterLockingDataMapper.getSftpFilePushSuccessSum(
                apiCode, bizDate);
        for (SftpFilePushSuccessDTO dto : successSum) {
            LocalFile localFile = new LocalFile();
            LocalFile localFileOld = localFileMapper.getByPrimaryKey(dto.getLocalId());
            if (ObjectUtils.isEmpty(localFileOld)) {
                continue;
            }
            boolean pushEndTimeBool = localFileOld.getPushEndTime() != null && bizDate.equals(LocalDateTime.ofInstant(
                    localFileOld.getPushEndTime().toInstant(), ZoneId.systemDefault())
                    .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            boolean isNotNull = localFileOld.getPushNumber() != null;
            boolean numberBool = isNotNull && (localFileOld.getPushNumber().equals(dto.getNumber())
                    || dto.getNumber() < localFileOld.getPushNumber());
            if (pushEndTimeBool && numberBool) {
                continue;
            }
            localFile.setPushNumber(localFileOld.getPushNumber() == null || pushEndTimeBool
                    ? dto.getNumber() : (localFileOld.getPushNumber() + dto.getNumber()));
            localFile.setId(dto.getLocalId());
            localFile.setPushEndTime(new Date());
            localFileMapper.updateByPrimaryKeySelective(localFile);
        }
    }

    public void localFilePushStatis(Long localId) {
        ZhonganRosterLockingDataExample lockingDataExample = new ZhonganRosterLockingDataExample();
        lockingDataExample.createCriteria().andLocalIdEqualTo(localId)
                .andPushStatusEqualTo(2)
                .andStatusEqualTo(1)
                .andDataSourceEqualTo(1);
        Integer count = zhonganRosterLockingDataMapper.countByExample(lockingDataExample);
        LocalFile localFile = new LocalFile();
        LocalFile localFileOld = localFileMapper.getByPrimaryKey(localId);
        if (!count.equals(localFileOld.getPushNumber())) {
            localFile.setPushNumber(count);
            localFile.setId(localId);
            localFile.setPushEndTime(new Date());
            localFileMapper.updateByPrimaryKeySelective(localFile);
        }

    }

    /**
     * 循环处理在当天拨打记录黑名单中,实时缓存
     */
    public void processTodayZhongaAnBlackData(Map<String,String> custNumMap,Set custNumBlackListSet,String nowDayStr){
        try{
            Iterator<Map.Entry<String, String>> iterator = custNumMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> ob = iterator.next();
                if (nowDayStr.equals(ob.getValue()) && redisChgService.sismember(RedisKeyConstant.zhongAnblackCusNumToday,ob.getKey())) {
                    iterator.remove();
                    custNumBlackListSet.add(ob.getKey() + nowDayStr);
                }
            }
        }catch (Exception e){
            log.error("redis查询众安当天拨打记录黑名单异常");
            processTodayZhongaAnBlackDataFromDb(custNumMap,custNumBlackListSet,nowDayStr);
        }
    }

    /**
     * 兼容redis不可用场景，查询db判断众安当天拨打记录黑名单
     */
    public void processTodayZhongaAnBlackDataFromDb(Map<String,String> custNumMap,Set custNumBlackListSet,String nowDayStr){
        if (CollectionUtils.isEmpty(custNumMap)) {
            return ;
        }
        Set<String> querySet = custNumMap.entrySet().stream()
                .filter(entry -> nowDayStr.equals(entry.getValue())).map(Map.Entry::getKey).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(querySet)) {
            return ;
        }
        long startMillis = System.currentTimeMillis();
        List<String> custNumList = callRecordMapper.getOneDayBlackListByCreateTime(querySet,nowDayStr);
        if(!CollectionUtils.isEmpty(custNumList)){
            custNumList.stream().forEach((String key) -> {
                custNumMap.remove(key);
                custNumBlackListSet.add(key + nowDayStr);
            });
        }
        log.warn("查询众安当天拨打记录黑名单总耗时:{}ms,数据{}条",System.currentTimeMillis() - startMillis,CollectionUtils.isEmpty(custNumList) ? 0 : custNumList.size());
    }
}
