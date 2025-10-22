package com.br.marketing.monkeydata.handle.zhongan;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.bo.ZaMarketDataBO;
import com.br.marketing.bo.ZhonganRosterLockingDataBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.ZhonganMarketingBanMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.monkeydata.query.ZhongAnCellZkDateQuery;
import com.br.marketing.monkeydata.query.ZhongAnMobileMd5BizDateQuery;
import com.br.marketing.monkeydata.service.Impl.DistributeSoleProcessor;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 推送名单锁定数据到众安
 * <p>部分逻辑从PushRosterLockingDataToZhongAnHandle直接迁移</p>
 * @dateTime 2024/03/09 11:43
 */
@Service
@Slf4j
public class ZhongAnPushRosterDataHandler extends IMonkeyDataHandle<ZhonganRosterLockingData
        , ZhonganRosterLockingDataBO, Page2Condition<ZhonganRosterLockingData>> {
    @Resource
    private ZhonganRosterLockingDataMapper zhonganRosterLockingDataMapper;

    @Resource
    private CallRecordMapper callRecordMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private ZhonganMarketingBanMapper zhonganMarketingBanMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private DistributeSoleProcessor distributeSoleProcessor;

    private static final String TITLE = "众安锁定名单推送";

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
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_REPORTEERROR.getCode(), e.getMessage()), e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }

    @Override
    public Result<?> customizedAction(Page2Condition<ZhonganRosterLockingData> condition) {
        Result<?> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());

        ThreadPoolExecutor pool = BrExecutors.getThreadPool(2, 2, 10);
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(24, 24, new SynchronousQueue<>());

        ZhonganRosterLockingData conditionParam = condition.getParam();

        // 营销日期集合, 分组条件查询, 条件 apiCode &bizDate &tag=CG、MG &pushStatus=1 &status=1 &sourceType=1,2
        Set<String> bizDates = zhonganRosterLockingDataMapper.getBizDateListtikv_(conditionParam);

        List<Future<Result<List<ZhonganRosterLockingDataBO>>>> futureList = new ArrayList<>();
        int pageIndex = condition.getPageIndex();
        int pageSize = condition.getPageSize();
        for (String bizDate : bizDates) {
            // 按照bizDate查询
            conditionParam.setBizDate(bizDate);
            conditionParam.setId(null);
            for (; ; ) {
                // 循环获取条件数据，每次2000条
                String userType = conditionParam.getUserType();
                if(conditionParam.getDataSource()==1){
                    conditionParam.setUserType("");
                }

                final List<ZhonganRosterLockingData> pageList = zhonganRosterLockingDataMapper.findPartColumnListPage(
                        conditionParam, pageIndex, pageSize);
                conditionParam.setUserType(userType);

                if (CollectionUtils.isEmpty(pageList)) {
                    break;
                }
                ZhonganRosterLockingData data = pageList.get(pageList.size() - 1);
                conditionParam.setId(data.getId());
                // TODO 后续线程池配置与业务逻辑分离
                setThreadPoolParam(pool, pushPool, conditionParam.getTag());

                ZhonganRosterLockingData pageParam = new ZhonganRosterLockingData();
                BeanUtils.copyProperties(conditionParam, pageParam);

                // 根据规则分类，推送数据
                futureList.add(pool.submit(() -> processData(pageList, pageParam, pushPool)));
            }
        }

        for (Future<Result<List<ZhonganRosterLockingDataBO>>> future : futureList) {
            try {
                future.get(1, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_REPORTEERROR.getCode(), e.getMessage()
                        , TITLE), e);
//                future.cancel(true);
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
                    log.warn("##业务线程等待超时{}", conditionParam.getTag());
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_REPORTEERROR.getCode(), e.getMessage()
                    , TITLE), e);
            result.setCode(ResultCode.FAIL.getValue());
            Thread.currentThread().interrupt();
        }

        taskCount = -1;
        pushPool.shutdown();
        try {
            while (!pushPool.awaitTermination(10, TimeUnit.SECONDS)) {
                long completedTask2Count = pushPool.getCompletedTaskCount();
                if (taskCount == completedTask2Count) {
                    log.warn("@@推送线程等待超时{}", conditionParam.getTag());
                    result.setCode(ResultCode.FAIL.getValue());
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_REPORTEERROR.getCode(), e.getMessage()
                    , TITLE), e);
            result.setCode(ResultCode.FAIL.getValue());
            Thread.currentThread().interrupt();
        }
        return result;
    }

    /**
     * distinctMobile
     */
//    private Set<String> distinctMobile(List<ZhonganRosterLockingData> pageList) {
//        Set<String> distinctMobileSet = new HashSet<>(2000);
//        Iterator<ZhonganRosterLockingData> iterator = pageList.iterator();
//        while (iterator.hasNext()) {
//            ZhonganRosterLockingData data = iterator.next();
//            // 组内去重
//            if (!distinctMobileSet.add(data.getMobileMd5())) {
//                iterator.remove();
//                continue;
//            }
//            // TODO 检查缓存是否已存在MobileMd5
//        }
//        return distinctMobileSet;
//    }

    @Override
    public Result<List<ZhonganRosterLockingDataBO>> processData(List<ZhonganRosterLockingData> inList) {
        return null;
    }

    public Result<List<ZhonganRosterLockingDataBO>> processData(List<ZhonganRosterLockingData> pageList,
            ZhonganRosterLockingData pageParam, ThreadPoolExecutor pushPool) {
        Result<List<ZhonganRosterLockingDataBO>> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());

        // pageParam
        String apiCode = pageParam.getApiCode();
        String tag = pageParam.getTag();
        String bizDate = pageParam.getBizDate();
        String userType = pageParam.getUserType();
        Integer dataSource = pageParam.getDataSource();

        try {
            // 分页内去重+缓存检查, 条件仅手机号
            // Set<String> distinctMobileSet = distinctMobile(pageList);

            Map<String, String> md5ToLogMap = md5ToLogMap(pageList);
            Set<String> cellSet = new HashSet<>(md5ToLogMap.values());

            // 有效期判断, 判断条件 cell + userType + bizDate
            // key: cell, value: SyncUserValidityPeriodsBO
            Map<String, SyncUserValidityPeriodsBO> cellToSyncUserBoMap = transferDataValidityPeriodService
                    .getValidityPeriodsByCellAndUserType(cellSet, userType, apiCode, bizDate);

            // 未获取到上传数据
            if (CollectionUtils.isEmpty(cellToSyncUserBoMap)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_VALIDITY_PERIOD.getCode(),
                        "apiCode:" + apiCode + ", bizDate:" + bizDate + ", tag:" + tag+ ", dataSource:" + dataSource
                                + ", userType:" + userType + "未获取到上传数据或未配置有效期！",
                        "锁定名单推送众安异常"));
                // if(judgeChangeStatus(pageParam)) {
                List<Long> ids = pageList.parallelStream().map(ZhonganRosterLockingData::getId).collect(Collectors.toList());
                updatePushStatusById(ids, 4);
                // }
                return result;
            }

            // 不在有效期内的id集合
            List<Long> notValidityIds = new ArrayList<>();
            // 未配置推送
            List<Long> notPushIds = new ArrayList<>();
            // 不营销
            List<Long> notMarketingIds = new ArrayList<>();
            // 黑名单
            List<Long> hitBlackIds = new ArrayList<>();
            // 重复数据
            List<Long> distributeIds = new ArrayList<>();

            // 有效期内assembleKeyToSyncUserMap, key: MobileMd5 + bizDate, value: MarketingSyncUser
            Map<String, MarketingSyncUser> keyToSyncUserMap = assembleKeyToSyncUserMap(pageList, md5ToLogMap,
                    cellToSyncUserBoMap, notValidityIds);

            List<ZhonganRosterLockingDataBO> pushList = new ArrayList<>();
            HashMap<String, JSONObject> pushConfig = marketingCommonConfig.getZhongAnDetailPush();
            Iterator<ZhonganRosterLockingData> iterator = pageList.iterator();
            switch (tag) {
                // 对照组
                case "CG":
                    processCG(pageList, pageParam, md5ToLogMap, cellToSyncUserBoMap, notPushIds, notMarketingIds,
                            pushList, pushConfig, iterator);
                    break;
                // 营销组
                case "MG":
                    processMG(pageList, pageParam, md5ToLogMap, cellToSyncUserBoMap, notPushIds, hitBlackIds,
                            keyToSyncUserMap, pushList, pushConfig);
                    break;
                default:
                    while (iterator.hasNext()) {
                        ZhonganRosterLockingData next = iterator.next();
                        String key = next.getMobileMd5() + next.getBizDate();
                        MarketingSyncUser user = keyToSyncUserMap.get(key);
                        if (ObjectUtils.isEmpty(user)) {
                            continue;
                        }
                        pushList.add(new ZhonganRosterLockingDataBO(next, user, apiCode, tag));
                    }
            }

            // distribute去重 cell + distribute_date
            distributeIds = distributeSoleProcessor.process(pushList, pageParam);

            log.warn(TITLE+"notValidityIds:{}",JSONObject.toJSON(notValidityIds));
            log.warn(TITLE+"notPushIds:{}",JSONObject.toJSON(notPushIds));
            log.warn(TITLE+"notMarketingIds:{}",JSONObject.toJSON(notMarketingIds));
            log.warn(TITLE+"hitBlackIds:{}",JSONObject.toJSON(hitBlackIds));
            log.warn(TITLE+"distributeIds:{}",JSONObject.toJSON(distributeIds));

            // if(judgeChangeStatus(pageParam)) {
            updatePushStatusById(notValidityIds, 4);
            // }
            updatePushStatusById(notPushIds, 8);
            updatePushStatusById(notMarketingIds, 7);
            updatePushStatusById(hitBlackIds, 5);
            updatePushStatusById(distributeIds, 6);
            if (CollectionUtils.isEmpty(pushList)) {
                return result;
            }

            Result<?> resultAction = resultAction(pushList, pushPool);
            result.setCode(resultAction.getCode());
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_REPORTEERROR.getCode(), e.getMessage()), e);
        }
        return result;
    }

    /**
     * 从历史@PushRosterLockingDataToZhongAnHandle迁移
     * <p>为配置sonar扫描，提取方法，参数较多</p>
     */
    private void processCG(List<ZhonganRosterLockingData> pageList, ZhonganRosterLockingData pageParam,
                           Map<String, String> md5ToLogMap, Map<String, SyncUserValidityPeriodsBO> cellToSyncUserBoMap,
                           List<Long> notPushIds, List<Long> notMarketingIds, List<ZhonganRosterLockingDataBO> pushList,
                           HashMap<String, JSONObject> pushConfig, Iterator<ZhonganRosterLockingData> iterator) {
        String apiCode = pageParam.getApiCode();
        String tag = pageParam.getTag();
        String userType = pageParam.getUserType();

        // 众安不营销记录表
        Set<String> cellZkDateMap = getMarketingBanMap(apiCode, pageList, md5ToLogMap);
        while (iterator.hasNext()) {
            ZhonganRosterLockingData next = iterator.next();

            collectNotPushIds(pushConfig, userType, notPushIds, next);

            // 不在有效期
            String cell = md5ToLogMap.getOrDefault(next.getMobileMd5(),"");
            SyncUserValidityPeriodsBO syncUserBO = cellToSyncUserBoMap.get(cell);
            if(syncUserBO==null || syncUserBO.getSyncUsers().size()<1){
                continue;
            }
            MarketingSyncUser syncUser = syncUserBO.getSyncUsers().get(0);

            if (cellZkDateMap.contains(cell + next.getBizDate())) {
                notMarketingIds.add(next.getId());
                continue;
            }
            pushList.add(new ZhonganRosterLockingDataBO(next, syncUser, apiCode, tag));
        }
    }

    /**
     * 从历史@PushRosterLockingDataToZhongAnHandle迁移
     * <p>为配置sonar扫描，提取方法，参数较多</p>
     */
    private void processMG(List<ZhonganRosterLockingData> pageList, ZhonganRosterLockingData pageParam,
            Map<String, String> md5ToLogMap, Map<String, SyncUserValidityPeriodsBO> cellToSyncUserBoMap,
            List<Long> notPushIds, List<Long> hitBlackIds, Map<String, MarketingSyncUser> keyToSyncUserMap,
                           List<ZhonganRosterLockingDataBO> pushList, HashMap<String, JSONObject> pushConfig) {
        Iterator<ZhonganRosterLockingData> iterator = pageList.iterator();
        String apiCode = pageParam.getApiCode();
        String tag = pageParam.getTag();
        String userType = pageParam.getUserType();

        Set<String> custNumBlackListSet = assembleBackList(pageList, keyToSyncUserMap, apiCode, cellToSyncUserBoMap);
        while (iterator.hasNext()) {
            ZhonganRosterLockingData next = iterator.next();

            collectNotPushIds(pushConfig, userType, notPushIds, next);

            // 不在有效期
            String cell = md5ToLogMap.getOrDefault(next.getMobileMd5(),"");
            SyncUserValidityPeriodsBO syncUserBO = cellToSyncUserBoMap.get(cell);
            if(syncUserBO==null || syncUserBO.getSyncUsers().size()<1){
                continue;
            }
            MarketingSyncUser syncUser = syncUserBO.getSyncUsers().get(0);

            // 判断黑名单
            if (custNumBlackListSet.contains(syncUser.getCustNum() + next.getBizDate())) {
                hitBlackIds.add(next.getId());
                continue;
            }
            pushList.add(new ZhonganRosterLockingDataBO(next, syncUser, apiCode, tag));
        }
    }

    private void collectNotPushIds(HashMap<String, JSONObject> pushConfig, String userType, List<Long> notPushIds,
        ZhonganRosterLockingData next){
        // 未配置可推送， 放入notPushIds
        if (!isEnablePushConfig(pushConfig, userType)) {
            notPushIds.add(next.getId());
        }
    }

    /**
     * isEnablePushConfig
     */
    private boolean isEnablePushConfig(HashMap<String, JSONObject> pushConfig, String userType) {
        JSONObject push = pushConfig.get(userType);
        if (push != null && "1".equals(push.getString("isPush"))) {
            return true;
        }
        return false;
    }

    /**
     * judgeChangeStatus
     * <p>判断status置为4的条件</p>
     */
    private boolean judgeChangeStatus(ZhonganRosterLockingData pageParam) {
        String tag = pageParam.getTag();
        String userType = pageParam.getUserType();
        Integer dataSource = pageParam.getDataSource();
        boolean a = "CG".equals(tag) && "1".equals(String.valueOf(dataSource)) && "1".equals(userType);
        boolean b = "MG".equals(tag) && "1".equals(String.valueOf(dataSource)) && "1".equals(userType);
        boolean c = "MG".equals(tag) && "2".equals(String.valueOf(dataSource)) && "8".equals(userType);
        return a || b || c;
    }

    /**
     * 有效期内keyToSyncUserMap
     * <p>key: MobileMd5 + bizDate, value: MarketingSyncUser</p>
     */
    private Map<String, MarketingSyncUser> assembleKeyToSyncUserMap(List<ZhonganRosterLockingData> pageList,
        Map<String, String> md5ToLogMap, Map<String, SyncUserValidityPeriodsBO> cellToSyncUserBOMap,
        List<Long> notValidityIds) {
        // 有效期内syncUserMapNew, key: MobileMd5 + bizDate
        Map<String, MarketingSyncUser> keyToSyncUserMap = pageList.stream().filter((ZhonganRosterLockingData data) -> {
            if (cellToSyncUserBOMap.containsKey(md5ToLogMap.get(data.getMobileMd5()))) {
                return true;
            }
            notValidityIds.add(data.getId());
            return false;
        })
        .filter(distinctByKey(ZhonganRosterLockingData::getMobileMd5, ZhonganRosterLockingData::getBizDate))
        .collect(Collectors.toConcurrentMap(
                (ZhonganRosterLockingData data) -> data.getMobileMd5() + data.getBizDate(),
                (ZhonganRosterLockingData data) -> {
                    SyncUserValidityPeriodsBO bo = cellToSyncUserBOMap.get(md5ToLogMap.get(data.getMobileMd5()));
                    if(bo!=null && bo.getSyncUsers().size()>0){
                        return bo.getSyncUsers().get(0);
                    }else {
                        return null;
                    }
                }));
        return keyToSyncUserMap;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor1, Function<? super T, Object> keyExtractor2) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(String.valueOf(keyExtractor1.apply(t))+String.valueOf(keyExtractor2.apply(t)), Boolean.TRUE) == null;
    }

    /**
     * MG组过滤CG组已推送 新版有效期
     */
    private Set<String> assembleBackList(List<ZhonganRosterLockingData> pageList
            , Map<String, MarketingSyncUser> keyToSyncUserMap
            , String apiCode
            , final Map<String, SyncUserValidityPeriodsBO> cellToSyncUserBOMap) {
        // key: cusNum, value: BizDate
        Map<String, String> custNumMap = new ConcurrentHashMap<>(1024);
        Map<String, PeriodOfValidityBO> custDayMap = new HashMap<>();
        // 众安客服拨打明细custNum今日黑名单
        Set<String> custNumBlackListSet = new HashSet<>();

        List<ZhongAnMobileMd5BizDateQuery> queries = pageList.stream()
                .filter((ZhonganRosterLockingData data) -> keyToSyncUserMap.containsKey(data.getMobileMd5() + data.getBizDate()))
                .map((ZhonganRosterLockingData data) -> {
                    MarketingSyncUser syncUser = keyToSyncUserMap.get(data.getMobileMd5() + data.getBizDate());
                    SyncUserValidityPeriodsBO periodsBo = cellToSyncUserBOMap.get(syncUser.getCell());
                    PeriodOfValidityBO bo = periodsBo.getBuilders().get(0).addOfDayTimeStrString().builder();
                    // key: custNum, value: BizDate
                    custNumMap.put(syncUser.getCustNum(), data.getBizDate());
                    // key: MobileMd5, value: PeriodOfValidityBO
                    custDayMap.put(data.getMobileMd5(), bo);
                    // 构造查询条件
                    return new ZhongAnMobileMd5BizDateQuery(data.getMobileMd5(), bo);
                }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(queries)) {
            return custNumBlackListSet;
        }

        if (CollectionUtils.isEmpty(pageList)) {
            return Collections.emptySet();
        }
        String nowDayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        processTodayZhongaAnBlackData(custNumMap,custNumBlackListSet, nowDayStr);
        if (!CollectionUtils.isEmpty(custNumMap)) {
            // 客服拨打记录表  callStatus≠12 12-黑名单
            List<String> bizDates = pageList.stream()
                    .map(ZhonganRosterLockingData::getBizDate)
                    .distinct()  // 去重
                    .collect(Collectors.toList());
            List<CallRecord> blackListSettikv_;
            if (bizDates.size() == 1) {
                blackListSettikv_ = callRecordMapper.getBlackListSetNewtikv_(custNumMap,bizDates.get(0));
            } else {
                blackListSettikv_ = callRecordMapper.getBlackListSettikv_(custNumMap, apiCode);
            }
            if (!CollectionUtils.isEmpty(blackListSettikv_)) {
                custNumBlackListSet.addAll(blackListSettikv_.stream()
                        .map(t -> t.getCaseNum() + new SimpleDateFormat("yyyy-MM-dd").format(t.getCallStartTime()))
                        .collect(Collectors.toSet()));
            }
        }
        return custNumBlackListSet;
    }

    /**
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
        List<ZaMarketDetail> pushList = new ArrayList<>();
        List<Long> pushIds = new ArrayList<>();
        for (ZhonganRosterLockingDataBO bo : outputDataList) {
            ZaMarketDetail detail = new ZaMarketDetail();
            ZhonganRosterLockingData data = bo.getData();
            pushIds.add(data.getId());
            MarketingSyncUser syncUser = bo.getSyncUser();
            String channelCode = "MG".equals(data.getTag()) || "CG".equals(data.getTag())
                    ? zhongAnDetailPush.get(syncUser.getUserType()).getString("channelCode")
                    : ZhongAnClient.XdChannelCode;
            detail.setBizDate(data.getBizDate());
            detail.setTaskId(syncUser.getCusBatch());
            detail.setChannelCode(channelCode);
            detail.setTag(data.getTag());
            detail.setMobileMd5(data.getMobileMd5());
            detail.setPostbackDate(DateUtil.formatDateTime(new Date()));
            detail.setIsOutbound(1);
            detail.setIsConnect(data.getIsConnect());
            detail.setIsSmsSend(data.getIsConnect());
            detail.setIsSmsSendSuccess(data.getIsConnect());
            pushList.add(detail);
            count++;

            if (pushList.size() == pushSize || size == count) {
                List<ZaMarketDetail> finalList = pushList;
                List<Long> finalPushIds = pushIds;
                pushPool.execute(() -> {
                    ZaMarketDataDTO dataDTO = new ZaMarketDataDTO();
                    dataDTO.setData(finalList);
                    methodRetryHandlerService.callZhongAnData(new ZaMarketDataBO(dataDTO
                            , bo.getApiCode(), bo.getTag(), finalPushIds), null);
                });
                pushList = new ArrayList<>();
                pushIds = new ArrayList<>();
            }
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    /**
     * <H3>手机号md5转log加密</H3>
     * <p>key: MobileMd5, value: cell log</p>
     */
    private Map<String, String> md5ToLogMap(List<ZhonganRosterLockingData> list) {
        return list.parallelStream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(Comparator.comparing(ZhonganRosterLockingData::getMobileMd5)))
                , ArrayList::new))
                .stream().collect(Collectors.toConcurrentMap(
                        ZhonganRosterLockingData::getMobileMd5, (ZhonganRosterLockingData d) -> {
            String query = RpcClientProxy.decode(d.getMobileMd5(), "cell", "md5", "");
            return StringUtils.isBlank(query) ? d.getMobileMd5() : BrCipherMaker.getInstance().encode(query);
        }, (v1, v2) -> v1));
    }

    /**
     * 配置线程池参数
     */
    private void setThreadPoolParam(ThreadPoolExecutor pool, ThreadPoolExecutor pushPool, String tag) {
        Map<String, List<Integer>> zhongAnPushTreadPoolMap = marketingCommonConfig.getZhongAnPushTreadPoolSizeMap();
        if(StringUtils.isEmpty(tag)){
            tag = "CG";
        }
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
