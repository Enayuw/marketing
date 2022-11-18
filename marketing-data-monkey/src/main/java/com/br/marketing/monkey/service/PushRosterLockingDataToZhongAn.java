package com.br.marketing.monkey.service;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.ZaMarketDataBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.monkey.bo.ZhonganRosterLockingDataBO;
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

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Override
    public Result<IterationResult<ZhonganRosterLockingData, Page2Condition<ZhonganRosterLockingData>>> getInputData(
            Page2Condition<ZhonganRosterLockingData> condition) {
        Result<IterationResult<ZhonganRosterLockingData, Page2Condition<ZhonganRosterLockingData>>> result
                = new Result<>();
        try {
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
        Map<String, String> cellMap = inList.parallelStream().collect(
                Collectors.toMap(ZhonganRosterLockingData::getMobileMd5, d -> {
                    String query = RpcClientProxy.decode(d.getMobileMd5(), "cell", "md5", "");
                    return StringUtils.isBlank(query) ? d.getMobileMd5() : BrCipherMaker.getInstance().encode(query);
                }));
        Set<String> mobileMd5Set = new HashSet<>(cellMap.values());
        Map<String, MarketingSyncUser> syncUserMap = marketingSyncUserService.getCellByCellAndMaxAppletTimeMap(apiCode
                , mobileMd5Set);
        boolean emptyBool = CollectionUtils.isEmpty(syncUserMap);
        if (emptyBool) {
            log.warn("tag:{},apiCode{},未获取到上传数据！", tag, apiCode);
            // 未获取到上传数据
            zhonganRosterLockingDataMapper.updatePushStatusORStatus(apiCode, null, 3
                    , 1, tag, inList, dateStr);
            return result;
        }
        Map<String, String> zhongAnPeriodOfValidityDay = marketingCommonConfig.getZhongAnPeriodOfValidityDay();
        if (CollectionUtils.isEmpty(zhongAnPeriodOfValidityDay) || zhongAnPeriodOfValidityDay.containsKey(apiCode)) {
            log.warn("tag:{},apiCode{},未配置有效期[zhongAnPeriodOfValidityDay]！", tag, apiCode);
            return result;
        }
        Map<String, MarketingSyncUser> syncUserMapNew = inList.parallelStream().filter(
                l -> syncUserMap.containsKey(cellMap.get(l.getMobileMd5())))
                .collect(Collectors.toMap(ZhonganRosterLockingData::getMobileMd5
                        , l -> syncUserMap.get(cellMap.get(l.getMobileMd5()))));
        Integer day;
        try {
            day = dataLoadingHandlerService.getPeriodOfValidityDay(zhongAnPeriodOfValidityDay, apiCode);
        } catch (IllegalAccessException e) {
            log.warn(e.getMessage(), e);
            return result;
        }
        boolean mgBool = "MG".equals(tag);
        Set<String> custNumBlackListSet = null;
        if (mgBool) {
            List<ZhongAnMobileMd5BizDateQuery> queries = inList.parallelStream().map(l -> {
                MarketingSyncUser syncUser = syncUserMapNew.get(l.getMobileMd5());
                PeriodOfValidityBO periodOfValidityBO = marketingSyncUserService.getPeriodOfValidityRange(day
                        , syncUser.getAppletTime() == null ? syncUser.getCreateTime()
                                : syncUser.getAppletTime()).addDateString().builder();
                return new ZhongAnMobileMd5BizDateQuery(l.getMobileMd5(), periodOfValidityBO);
            }).collect(Collectors.toList());
            Set<String> cgMobileMd5Set = zhonganRosterLockingDataMapper.getMobileMd5ByBeforePushSet(queries, apiCode, "CG");
            if (!CollectionUtils.isEmpty(cgMobileMd5Set)) {
                // 过滤CG组是否已经推送过
                List<ZhonganRosterLockingData> list = inList.parallelStream().filter(
                        l -> cgMobileMd5Set.contains(l.getMobileMd5())).collect(Collectors.toList());
                // 去掉CG组已推送
                inList.removeAll(list);
                // 重复数据
                zhonganRosterLockingDataMapper.updatePushStatusORStatus(apiCode, null, 6
                        , 1, tag, list, dateStr);
            }
            Set<String> custNumSet = syncUserMapNew.values().parallelStream().map(MarketingSyncUser::getCustNum)
                    .collect(Collectors.toSet());
            Set<String> custNumCache = redisChgService.smembers(RedisKeyConstant.zhongAnblackCusNumToday);
            custNumBlackListSet = new HashSet<>(custNumSet);
            custNumBlackListSet.retainAll(custNumCache);
            custNumSet.removeAll(custNumBlackListSet);
            custNumBlackListSet.addAll(callRecordMapper.getBlackListSet(custNumSet, apiCode, dateStr));
        }
        Iterator<ZhonganRosterLockingData> iterator = inList.iterator();
        List<ZhonganRosterLockingDataBO> list = new ArrayList<>();
        while (iterator.hasNext()) {
            ZhonganRosterLockingData next = iterator.next();
            String mobileMd5 = next.getMobileMd5();
            if (syncUserMapNew.containsKey(mobileMd5)) {
                String bizDateStr = next.getBizDate();
                LocalDate bizDate;
                try {
                    bizDate = LocalDate.parse(bizDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    bizDate = LocalDateTime.parse(bizDateStr, DateTimeFormatter.ofPattern(
                            DateHelper.LINE_DATE_COLON_TIME_FORMAT)).toLocalDate();
                }
                MarketingSyncUser syncUser = syncUserMapNew.get(mobileMd5);
                Date date = Date.from(bizDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                Date validityDate = syncUser.getAppletTime() == null ? syncUser.getCreateTime()
                        : syncUser.getAppletTime();
                boolean validityBool = marketingSyncUserService.isPeriodOfValidity(date, day, validityDate);
                if (validityBool) {
                    //营销组
                    if (mgBool) {
                        // 判断黑名单
                        if (custNumBlackListSet.contains(syncUser.getCustNum())) {
                            // 命中黑名单
                            updatePushStatus(next, 5, dateStr);
                            continue;
                        }
                    }
                    list.add(new ZhonganRosterLockingDataBO(next, syncUser, apiCode, tag));
                } else {
                    // 不在有效期内
                    updatePushStatus(next, 4, dateStr);
                }
            } else {
                // 未获取到上传数据
                updatePushStatus(next, 3, dateStr);
            }
        }
        if (!CollectionUtils.isEmpty(list)) {
            result.setDate(list);
            result.setCode(ResultCode.SUCCESS.getValue());
        }
        return result;
    }

    private void updatePushStatus(ZhonganRosterLockingData data, int updateStatus, String dateStr) {
        List<ZhonganRosterLockingData> dataList = new ArrayList<>();
        dataList.add(data);
        zhonganRosterLockingDataMapper.updatePushStatusORStatus(data.getApiCode(), null, updateStatus
                , 1, data.getTag(), dataList, dateStr);
    }

    @Override
    public Result<?> resultAction(List<ZhonganRosterLockingDataBO> outputDataList) {
        Result<Object> result = new Result<>();
        if (CollectionUtils.isEmpty(outputDataList)) {
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }
        int size = outputDataList.size();
        int pushSize = 100;
        int count = 1;
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
            if (list.size() == pushSize || (size - (count * pushSize)) <= pushSize) {
                ZaMarketDataDTO dataDTO = new ZaMarketDataDTO();
                dataDTO.setData(list);
                methodRetryHandlerService.callZhongAnData(new ZaMarketDataBO(dataDTO, bo.getApiCode(), bo.getTag()
                        , dataList), null);
                list.clear();
                dataList.clear();
            }
            count++;
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

}
