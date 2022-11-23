package com.br.marketing.monkeydata.service;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.ZaMarketDataBO;
import com.br.marketing.bo.ZhonganRosterLockingDataBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.monkeydata.query.ZhongAnMobileMd5BizDateQuery;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
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
public class PushRosterLockingDataToZhongAn extends IMonkeyDataHandle<ZhonganRosterLockingData
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
    private DataLoadingHandlerService dataLoadingHandlerService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private static final ThreadPoolExecutor POOL = BrExecutors.getThreadPool(25, 50);

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
    public Result<List<ZhonganRosterLockingDataBO>> processData(List<ZhonganRosterLockingData> inList) {
        Result<List<ZhonganRosterLockingDataBO>> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());
        if (inList == null || inList.size() < 1) {
            return result;
        }
        ZhonganRosterLockingData data = inList.get(0);
        String apiCode = data.getApiCode();
        String tag = data.getTag();
        String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Map<String, String> cellMap = md5ToLogMap(inList);
        Set<String> mobileMd5Set = new HashSet<>(cellMap.values());
        Map<String, MarketingSyncUser> syncUserMap = marketingSyncUserService.getCellByCellAndMaxAppletTimeMap(apiCode
                , mobileMd5Set);
        boolean emptyBool = CollectionUtils.isEmpty(syncUserMap);
        if (emptyBool) {
            log.warn("tag:{},apiCode{},未获取到上传数据！", tag, apiCode);
            // 未获取到上传数据
            updatePushStatus(inList, 3, apiCode, tag, dateStr);
            return result;
        }
        Map<String, String> zhongAnPeriodOfValidityDay = marketingCommonConfig.getZhongAnPeriodOfValidityDay();
        if (CollectionUtils.isEmpty(zhongAnPeriodOfValidityDay) || !zhongAnPeriodOfValidityDay.containsKey(apiCode)) {
            log.warn("tag:{},apiCode{},未配置有效期[zhongAnPeriodOfValidityDay]！", tag, apiCode);
            return result;
        }
        Map<String, MarketingSyncUser> syncUserMapNew = inList.parallelStream().filter(l -> syncUserMap.containsKey(
                cellMap.get(l.getMobileMd5()))).collect(Collectors.toMap(d -> d.getMobileMd5() + d.getBizDate()
                , l -> syncUserMap.get(cellMap.get(l.getMobileMd5()))));
        Integer day;
        try {
            day = dataLoadingHandlerService.getPeriodOfValidityDay(zhongAnPeriodOfValidityDay, apiCode);
        } catch (IllegalAccessException e) {
            log.warn(e.getMessage(), e);
            return result;
        }
        Iterator<ZhonganRosterLockingData> iterator = inList.iterator();
        List<ZhonganRosterLockingDataBO> list = new ArrayList<>();
        List<ZhonganRosterLockingData> hitBlackList = new ArrayList<>();
        List<ZhonganRosterLockingData> notValidity = new ArrayList<>();
        List<ZhonganRosterLockingData> notUploadData = new ArrayList<>();
        MarketingSyncUser syncUser;
        switch (tag) {
            case "CG":
                // 对照组
                while (iterator.hasNext()) {
                    ZhonganRosterLockingData next = iterator.next();
                    if ((syncUser = periodOfValidity(syncUserMapNew, day, next, notValidity, notUploadData)) != null) {
                        list.add(new ZhonganRosterLockingDataBO(next, syncUser, apiCode, tag));
                    }
                }
                break;
            case "MG":
                // 营销组
                Set<String> custNumBlackListSet = mgFilterCgPush(inList, syncUserMapNew, apiCode, tag, dateStr, day);
                iterator = inList.iterator();
                while (iterator.hasNext()) {
                    ZhonganRosterLockingData next = iterator.next();
                    if ((syncUser = periodOfValidity(syncUserMapNew, day, next, notValidity, notUploadData)) != null) {
                        // 判断黑名单
                        if (custNumBlackListSet.contains(syncUser.getCustNum())) {
                            // 命中黑名单
                            hitBlackList.add(next);
                            continue;
                        }
                        list.add(new ZhonganRosterLockingDataBO(next, syncUser, apiCode, tag));
                    }
                }
            default:
        }
        updatePushStatus(hitBlackList, 5, apiCode, tag, dateStr);
        updatePushStatus(notValidity, 4, apiCode, tag, dateStr);
        updatePushStatus(notUploadData, 3, apiCode, tag, dateStr);
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(list);
        return result;
    }

    /**
     * 2022/11/19 13:40
     * 有效期判断
     */
    private MarketingSyncUser periodOfValidity(Map<String, MarketingSyncUser> syncUserMapNew
            , Integer day
            , ZhonganRosterLockingData next
            , List<ZhonganRosterLockingData> notValidity
            , List<ZhonganRosterLockingData> notUploadData) {
        String mobileMd5 = next.getMobileMd5();
        String key = mobileMd5 + next.getBizDate();
        if (syncUserMapNew.containsKey(key)) {
            MarketingSyncUser syncUser = syncUserMapNew.get(key);
            Date validityDate = syncUser.getAppletTime() == null ? syncUser.getCreateTime() : syncUser.getAppletTime();
            boolean validityBool = marketingSyncUserService.isPeriodOfValidity(new Date(), day, validityDate);
            if (validityBool) {
                return syncUser;
            } else {
                // 不在有效期内
                notValidity.add(next);
            }
        } else {
            // 未获取到上传数据
            notUploadData.add(next);
        }
        return null;
    }

    /**
     * 2022/11/19 13:41
     * MG组过滤CG组已推送
     */
    private Set<String> mgFilterCgPush(List<ZhonganRosterLockingData> inList
            , Map<String, MarketingSyncUser> syncUserMapNew
            , String apiCode
            , String tag
            , String dateStr
            , int day) {
        List<ZhongAnMobileMd5BizDateQuery> queries = inList.parallelStream().filter(
                l -> syncUserMapNew.containsKey(l.getMobileMd5() + l.getBizDate())).map(l -> {
            MarketingSyncUser syncUser = syncUserMapNew.get(l.getMobileMd5() + l.getBizDate());
            PeriodOfValidityBO periodOfValidityBO = marketingSyncUserService.getPeriodOfValidityRange(day
                    , syncUser.getAppletTime() == null ? syncUser.getCreateTime()
                            : syncUser.getAppletTime()).addDateString().builder();
            return new ZhongAnMobileMd5BizDateQuery(l.getMobileMd5(), periodOfValidityBO);
        }).collect(Collectors.toList());
        Set<String> cgMobileMd5Set = zhonganRosterLockingDataMapper.getMobileMd5ByBeforePushSettikv_(queries
                , apiCode, "CG");
        if (!CollectionUtils.isEmpty(cgMobileMd5Set)) {
            // 过滤CG组是否已经推送过
            List<ZhonganRosterLockingData> list = inList.parallelStream().filter(
                    l -> cgMobileMd5Set.contains(l.getMobileMd5())).collect(Collectors.toList());
            // 去掉CG组已推送
            inList.removeAll(list);
            // 重复数据
            updatePushStatus(list, 6, apiCode, tag, dateStr);
        }
        if (CollectionUtils.isEmpty(inList)) {
            return Collections.emptySet();
        }
        Set<String> custNumSet = syncUserMapNew.values().parallelStream().map(MarketingSyncUser::getCustNum)
                .collect(Collectors.toSet());
        Set<String> custNumCache = redisChgService.smembers(RedisKeyConstant.zhongAnblackCusNumToday);
        Set<String> custNumBlackListSet = new HashSet<>(custNumSet);
        custNumBlackListSet.retainAll(custNumCache);
        custNumSet.removeAll(custNumBlackListSet);
        custNumBlackListSet.addAll(callRecordMapper.getBlackListSettikv_(custNumSet, apiCode, dateStr));
        return custNumBlackListSet;
    }

    /**
     * 2022/11/19 10:55
     * 更新数据状态
     */
    private void updatePushStatus(List<ZhonganRosterLockingData> dataList
            , int updateStatus
            , String apiCode
            , String tag
            , String dateStr) {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        zhonganRosterLockingDataMapper.updatePushStatusOrStatus(apiCode, null, updateStatus
                , 1, tag, dataList, dateStr, new Date());
    }

    @Override
    public Result<?> resultAction(List<ZhonganRosterLockingDataBO> outputDataList) {
        Result<Object> result = new Result<>();
        if (CollectionUtils.isEmpty(outputDataList)) {
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }
        CompletionService<Result<?>> completionService = getCompletionService();
        int size = outputDataList.size();
        int pushSize = 100;
        int count = 0;
        List<ZaMarketDetail> list = new ArrayList<>();
        List<ZhonganRosterLockingData> dataList = new ArrayList<>();
        for (ZhonganRosterLockingDataBO bo : outputDataList) {
            ZaMarketDetail detail = new ZaMarketDetail();
            ZhonganRosterLockingData data = bo.getData();
            dataList.add(data);
            MarketingSyncUser syncUser = bo.getSyncUser();
            detail.setBizDate(data.getBizDate());
            detail.setTaskId(syncUser.getCusBatch());
            detail.setChannelCode(ZhongAnClient.XdChannelCode);
            detail.setTag(data.getTag());
            detail.setMobileMd5(data.getMobileMd5());
            list.add(detail);
            count++;
            if (list.size() == pushSize || size == count) {
                List<ZaMarketDetail> finalList = list;
                List<ZhonganRosterLockingData> finalDataList = dataList;
                completionService.submit(() -> {
                    ZaMarketDataDTO dataDTO = new ZaMarketDataDTO();
                    dataDTO.setData(finalList);
                    return methodRetryHandlerService.callZhongAnData(new ZaMarketDataBO(dataDTO
                            , bo.getApiCode(), bo.getTag(), finalDataList), null);
                });
                list = new ArrayList<>();
                dataList = new ArrayList<>();
            }
        }
        try {
            int pageTotal = size % pushSize == 0 ? size / pushSize : (size / pushSize + 1);
            for (; pageTotal > 0; pageTotal--) {
                Result<?> result1 = completionService.take().get(5, TimeUnit.SECONDS);
                log.warn("##众安名单锁定多线程推送任务结果[总数-总页数-响应code]：{}-{}-{}", count, pageTotal
                        , result1.getCode());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error(e.getMessage(), e);
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
                , ArrayList::new)).parallelStream().collect(Collectors.toMap(ZhonganRosterLockingData::getMobileMd5, d -> {
            String query = RpcClientProxy.decode(d.getMobileMd5(), "cell", "md5", "");
            return StringUtils.isBlank(query) ? d.getMobileMd5() : BrCipherMaker.getInstance().encode(query);
        }, (v1, v2) -> v1));
    }

    /**
     * 2022/11/22 17:40
     * 配置线程
     */
    private CompletionService<Result<?>> getCompletionService() {
        List<Integer> zhongAnPushTreadPoolSize = marketingCommonConfig.getZhongAnPushTreadPoolSize();
        CompletionService<Result<?>> service = new ExecutorCompletionService<>(POOL);
        if (CollectionUtils.isEmpty(zhongAnPushTreadPoolSize)) {
            return service;
        }
        int size = zhongAnPushTreadPoolSize.size();
        int corePoolSize;
        int maximumPoolSize;
        if (size == 1) {
            Integer poolSize = zhongAnPushTreadPoolSize.get(0);
            if (ObjectUtils.isEmpty(poolSize)) {
                return service;
            }
            corePoolSize = poolSize;
            maximumPoolSize = poolSize;
        } else if (size > 1) {
            Integer corePoolSizeNew = zhongAnPushTreadPoolSize.get(0);
            Integer maximumPoolSizeNew = zhongAnPushTreadPoolSize.get(0);
            if (ObjectUtils.isEmpty(corePoolSizeNew) && ObjectUtils.isEmpty(maximumPoolSizeNew)) {
                return service;
            }
            corePoolSize = corePoolSizeNew;
            maximumPoolSize = maximumPoolSizeNew;
        } else {
            return service;
        }
        int maxPoolSize = 1000;
        POOL.setCorePoolSize(Math.min(corePoolSize, maxPoolSize));
        POOL.setMaximumPoolSize(Math.min(maximumPoolSize, maxPoolSize));
        return service;
    }
}
