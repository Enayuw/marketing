package com.br.marketing.monkeydata.service;

import com.alibaba.fastjson.JSONObject;
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
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.exception.BusinessException;
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
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.IMarketingDataValidService;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private ZhonganMarketingBanMapper zhonganMarketingBanMapper;

    @Autowired
    IMarketingDataValidService iMarketingDataValidService;

    private static final ThreadPoolExecutor POOL = BrExecutors.getThreadPool(15, 20);

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
    public Result<List<ZhonganRosterLockingDataBO>> processData(List<ZhonganRosterLockingData> inList)
            throws IllegalAccessException {
        Result<List<ZhonganRosterLockingDataBO>> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());
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

        HashMap<String, JSONObject> zhongAnDetailPush = marketingCommonConfig.getZhongAnDetailPush();
        Result<List<MarketingDataValidConfig>> dataValidConfigByType = iMarketingDataValidService.getDataValidConfigByType(apiCode, 3);
        AssertResult.assertResult(dataValidConfigByType);
        List<MarketingDataValidConfig> validConfigs = dataValidConfigByType.getData();
        Map<String, Integer> userTypeDays = validConfigs.stream().collect(Collectors.toMap(MarketingDataValidConfig::getUserType
                , t -> dataLoadingHandlerService.periodOfValidityDay(t.getValidDays())));

        Map<String, MarketingSyncUser> syncUserMapNew = inList.stream().filter(l -> syncUserMap.containsKey(
                cellMap.get(l.getMobileMd5()))).collect(Collectors.toMap(d -> d.getMobileMd5() + d.getBizDate()
                , l -> syncUserMap.get(cellMap.get(l.getMobileMd5()))));

        Iterator<ZhonganRosterLockingData> iterator = inList.iterator();
        List<ZhonganRosterLockingDataBO> list = new ArrayList<>();
        //失效集合
        List<ZhonganRosterLockingData> notValidity = new ArrayList<>();
        //没有匹配上传数据集合
        List<ZhonganRosterLockingData> notUploadData = new ArrayList<>();
        //未配置有效期配置的数据
        List<ZhonganRosterLockingData> notValidConfigData = new ArrayList<>();
        //无需推送场景集合
        List<ZhonganRosterLockingData> notPushData = new ArrayList<>();
        MarketingSyncUser syncUser;
        switch (tag) {
            case "CG":
                // 对照组
                Set<String> cellZkDateMap = getMarketingBanMap(apiCode, inList, cellMap);
                List<ZhonganRosterLockingData> notMarketingList = new ArrayList<>();
                while (iterator.hasNext()) {
                    ZhonganRosterLockingData next = iterator.next();
                    if ((syncUser = isPush(syncUserMapNew, userTypeDays, zhongAnDetailPush, next, notValidity, notUploadData, notPushData, notValidConfigData)) != null) {
                        String cell = cellMap.getOrDefault(next.getMobileMd5(), "");
                        if (cellZkDateMap.contains(cell + next.getBizDate())) {
                            // 不营销
                            notMarketingList.add(next);
                            continue;
                        }
                        list.add(new ZhonganRosterLockingDataBO(next, syncUser, apiCode, tag));
                    }
                }
                updatePushStatus(notMarketingList, 7, apiCode, tag, dateStr);
                break;
            case "MG":
                // 营销组
                Set<String> custNumBlackListSet = mgFilterCgPush(inList, syncUserMapNew, apiCode, tag, dateStr, userTypeDays);
                iterator = inList.iterator();
                List<ZhonganRosterLockingData> hitBlackList = new ArrayList<>();
                while (iterator.hasNext()) {
                    ZhonganRosterLockingData next = iterator.next();
                    if ((syncUser = isPush(syncUserMapNew, userTypeDays, zhongAnDetailPush, next, notValidity, notUploadData, notPushData, notValidConfigData)) != null) {
                        // 判断黑名单
                        if (custNumBlackListSet.contains(syncUser.getCustNum()+next.getBizDate())) {
                            // 命中黑名单
                            hitBlackList.add(next);
                            continue;
                        }
                        list.add(new ZhonganRosterLockingDataBO(next, syncUser, apiCode, tag));
                    }
                }
                updatePushStatus(hitBlackList, 5, apiCode, tag, dateStr);
                break;
            default:
                while (iterator.hasNext()) {
                    ZhonganRosterLockingData next = iterator.next();
                    String key = next.getMobileMd5() + next.getBizDate();
                    MarketingSyncUser user = syncUserMapNew.get(key);
                    if (ObjectUtils.isEmpty(user)) {
                        notUploadData.add(next);
                    } else {
                        list.add(new ZhonganRosterLockingDataBO(next, user, apiCode, tag));
                    }
                }
        }
        updatePushStatus(notValidity, 4, apiCode, tag, dateStr);
        updatePushStatus(notUploadData, 3, apiCode, tag, dateStr);
        updatePushStatus(notPushData, 8, apiCode, tag, dateStr);
        updatePushStatus(notValidConfigData, 9, apiCode, tag, dateStr);
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(list);
        return result;
    }

    /**
     * 2022/11/19 13:40
     * 是否推送
     */
    private MarketingSyncUser isPush(Map<String, MarketingSyncUser> syncUserMapNew
            , Map<String, Integer> userTypeDay
            , HashMap<String, JSONObject> pushConfig
            , ZhonganRosterLockingData next
            , List<ZhonganRosterLockingData> notValidity
            , List<ZhonganRosterLockingData> notUploadData
            , List<ZhonganRosterLockingData> noPushData
            , List<ZhonganRosterLockingData> notValidConfigData) {
        String mobileMd5 = next.getMobileMd5();
        String key = mobileMd5 + next.getBizDate();
        // 未获取到上传数据
        if (!syncUserMapNew.containsKey(key)) {
            notUploadData.add(next);
            return null;
        }
        MarketingSyncUser syncUser = syncUserMapNew.get(key);
        JSONObject push = pushConfig.get(syncUser.getUserType());
        // 未配置可推送
        if (push == null || !"1".equals(push.getString("isPush"))) {
            noPushData.add(next);
            return null;
        }
        if (userTypeDay.get(syncUser.getUserType()) == null) {
            notValidConfigData.add(next);
            return null;
        }
        Result validByThreeType = iMarketingDataValidService.isValidByThreeType(userTypeDay, syncUser);
        // 不在有效期内
        if (!ResultCode.SUCCESS.getValue().equals(validByThreeType.getCode())) {
            notValidity.add(next);
            return null;
        }
        return syncUser;
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
            , Map<String, Integer> userTypeDays) {
        Map<String, String> custNumMap = new ConcurrentHashMap<>(1024);
        Set<String> custNumBlackListSet = new HashSet<>();
        String nowDay = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ZhongAnMobileMd5BizDateQuery> queries = inList.stream()
                .filter(l -> {
                    if (!syncUserMapNew.containsKey(l.getMobileMd5() + l.getBizDate())) {
                        return false;
                    }
                    MarketingSyncUser syncUser = syncUserMapNew.get(l.getMobileMd5() + l.getBizDate());
                    Integer integer = userTypeDays.get(syncUser.getUserType());
                    if (integer == null) {
                        return false;
                    }
                    return true;
                })
                .map(l -> {
                    MarketingSyncUser syncUser = syncUserMapNew.get(l.getMobileMd5() + l.getBizDate());
                    custNumMap.put(syncUser.getCustNum(), l.getBizDate());
                    PeriodOfValidityBO periodOfValidityBO = marketingSyncUserService.getPeriodOfValidityRange(
                            userTypeDays.get(syncUser.getUserType())
                            , syncUser.getAppletTime() == null
                                    ? syncUser.getCreateTime()
                                    : syncUser.getAppletTime()).addDateString().builder();
                    return new ZhongAnMobileMd5BizDateQuery(l.getMobileMd5(), periodOfValidityBO);
                })
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(queries)){
            return custNumBlackListSet;
        }
        Set<String> cgMobileMd5Set = zhonganRosterLockingDataMapper.getMobileMd5ByBeforePushSettikv_(queries, apiCode, "CG");
        if (!CollectionUtils.isEmpty(cgMobileMd5Set)) {
            // 过滤CG组是否已经推送过
            List<ZhonganRosterLockingData> list = inList.stream().filter(
                    l -> cgMobileMd5Set.contains(l.getMobileMd5())).collect(Collectors.toList());
            // 去掉CG组已推送
            inList.removeAll(list);
            // 重复数据
            updatePushStatus(list, 6, apiCode, tag, dateStr);
        }
        if (CollectionUtils.isEmpty(inList)) {
            return Collections.emptySet();
        }
//        Set<String> custNumSet = syncUserMapNew.values().parallelStream().map(MarketingSyncUser::getCustNum)
//                .collect(Collectors.toSet());
        Set<String> custNumCache = redisChgService.smembers(RedisKeyConstant.zhongAnblackCusNumToday);
        Iterator<Map.Entry<String, String>> iterator = custNumMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> ob = iterator.next();
            if(nowDay.equals(ob.getValue())||custNumCache.contains(ob.getKey())){
                iterator.remove();
                custNumBlackListSet.add(ob.getKey()+nowDay);
            }
        }
//        Set<String> custNumBlackListSet = new HashSet<>(custNumSet);
//        custNumBlackListSet.retainAll(custNumCache);
//        custNumSet.removeAll(custNumBlackListSet);
//        if (CollectionUtils.isEmpty(custNumSet)) {
//            return custNumBlackListSet;
//        }
//        Set<String> set = custNumMap.keySet();
//        set.retainAll(custNumSet);
//        if (CollectionUtils.isEmpty(custNumMap)) {
//            return custNumBlackListSet;
//        }
        List<CallRecord> blackListSettikv_ = callRecordMapper.getBlackListSettikv_(custNumMap, apiCode);
        if(!CollectionUtils.isEmpty(blackListSettikv_)){
            custNumBlackListSet.addAll(blackListSettikv_.stream()
                    .map(t->t.getCaseNum()+new SimpleDateFormat("yyyy-MM-dd").format(t.getCallStartTime()))
                    .collect(Collectors.toSet()));
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
        HashMap<String, JSONObject> zhongAnDetailPush = marketingCommonConfig.getZhongAnDetailPush();
        CompletionService<Result<?>> completionService = getCompletionService();
        boolean isUseThreadPool = completionService != null;
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
            String channelCode = "MG".equals(data.getTag())||"CG".equals(data.getTag())
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
                if (isUseThreadPool) {
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
                } else {
                    ZaMarketDataDTO dataDTO = new ZaMarketDataDTO();
                    dataDTO.setData(list);
                    methodRetryHandlerService.callZhongAnData(new ZaMarketDataBO(dataDTO
                            , bo.getApiCode(), bo.getTag(), dataList), null);
                    list.clear();
                    dataList.clear();
                }
            }
        }
        if (isUseThreadPool) {
            try {
                int pageTotal = size % pushSize == 0 ? size / pushSize : (size / pushSize + 1);
                for (; pageTotal > 0; pageTotal--) {
                    completionService.take().get(10, TimeUnit.SECONDS);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(e.getMessage(), e);
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
                , ArrayList::new)).stream().collect(Collectors.toMap(ZhonganRosterLockingData::getMobileMd5, d -> {
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
        if (zhongAnPushTreadPoolSize == null) {
            return null;
        }
        CompletionService<Result<?>> service = new ExecutorCompletionService<>(POOL);
        int size = zhongAnPushTreadPoolSize.size();
        int corePoolSize;
        int maximumPoolSize;
        if (size == 1) {
            Integer poolSize = zhongAnPushTreadPoolSize.get(0);
            if (ObjectUtils.isEmpty(poolSize) || poolSize < 1) {
                return service;
            }
            corePoolSize = poolSize;
            maximumPoolSize = poolSize;
        } else if (size > 1) {
            Integer corePoolSizeNew = zhongAnPushTreadPoolSize.get(0);
            Integer maximumPoolSizeNew = zhongAnPushTreadPoolSize.get(0);
            if (ObjectUtils.isEmpty(corePoolSizeNew) || ObjectUtils.isEmpty(maximumPoolSizeNew)
                    || corePoolSizeNew < 1 || maximumPoolSizeNew < 1) {
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
        if (count != null && !count.equals(localFileOld.getPushNumber())) {
            localFile.setPushNumber(count);
            localFile.setId(localId);
            localFile.setPushEndTime(new Date());
            localFileMapper.updateByPrimaryKeySelective(localFile);
        }

    }
}
