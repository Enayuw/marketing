package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.auth.AuthShowProductor;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.msg.mq.ApiDataInfoDTO;
import com.br.marketing.dto.msg.mq.UserTypeCollectionDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferSyncReportMapper;
import com.br.marketing.mapper.VariableDicMapper;
import com.br.marketing.service.ICompatibleService;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.service.TransferSyncReportService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.vo.TransferSyncReportNumVO;
import com.br.marketing.vo.TransferSyncReportVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 转化数据报表实现
 *
 * @author Guo Zeqiang
 * @dateTime 2022/6/29 10:30
 */
@Service
@Slf4j
public class TransferSyncReportServiceImpl implements TransferSyncReportService {
    @Resource
    private TransferSyncReportMapper transferSyncReportMapper;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private VariableDicMapper variableDicMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    ICompatibleService iCompatibleService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private MarketingCustomerService marketingCustomerService;

    @Resource
    private PlatformTransactionManager platformTransactionManager;


    @Override
    public void reportProcess(Set<String> dateStrSet, int shardingTotalCount, List<Integer> shardingItems, String JobName) {
        long l = System.currentTimeMillis();
        // 分片获取所有客户
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(AuthShowProductor.NORMAL.getCode().byteValue());
        List<MarketingCustomer> customers = marketingCustomerMapper.selectByExampleAndShard(customerExample
                , shardingTotalCount, shardingItems);
        Map<String, Set<String>> userTypeMapByApiCode = getUserTypeMapByApiCode("");
        String other = "";
        Integer transferThreadNum = marketingCommonConfig.getSyncReportThreadConfig().getInteger("transfer");
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(transferThreadNum, transferThreadNum);
        List<String> smyApiCodes = (marketingCommonConfig.getSaMoYeTransferFileApiCodes() == null
                || marketingCommonConfig.getSaMoYeTransferFileApiCodes().size() <= 0)
                ? Arrays.asList("3710013")
                : marketingCommonConfig.getSaMoYeTransferFileApiCodes();
        for (String dateStr : dateStrSet) {
            for (MarketingCustomer customer : customers) {
                if (StringUtils.isNoneBlank(JobName)) {
                    Boolean action = iCompatibleService.isAction(customer.getExtendConfigInfo(), JobName);
                    if (!action) {
                        continue;
                    }
                }
                String apiCode = customer.getApiCode();
                String tCid = Optional.ofNullable(customer.getCid()).orElse(other).replace("-", other);
                boolean smy = smyApiCodes.contains(apiCode);
                // 获取场景
                Set<String> userTypeSet = userTypeMapByApiCode.getOrDefault(apiCode, Collections.emptySet());
                for (String userType : userTypeSet) {
                    String startDate = dateStr;
                    String endDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            .plusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    List<String> requestDateList = new ArrayList<>();
                    try {
                        requestDateList = smy ? Arrays.asList(startDate) : transferSyncReportMapper.requestDatetikv_(tCid, apiCode, startDate, endDate, userType);
                    } catch (Exception ex) {
                        continue;
                    }
                    for (String requestDate : requestDateList) {
                        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(
                            threadPool,
                            marketingCommonConfig.getSyncReportThreadConfig().getInteger("transfer")
                        );
                        threadPool.submit(() -> {
                            TransferSyncReport report = smy ? transferSyncReportMapper.dateTimeMinMaxCountSMYtiflash_(apiCode, requestDate, userType)
                                    : transferSyncReportMapper.dateTimeMinMaxCounttiflash_(tCid, apiCode, requestDate, userType);
                            try {
                                Date appletBeginTime = report.getAppletBeginTime();
                                Date appletEndTime = report.getAppletEndTime();
                                Integer dataCount = report.getDataCount();
                                if (appletBeginTime == null || appletEndTime == null || dataCount == null || dataCount < 1) {
                                    return;
                                }
                                // 检索历史记录
                                TransferSyncReportExample example = new TransferSyncReportExample();
                                example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                                        .andAppletDateEqualTo(requestDate);
                                List<TransferSyncReport> list = findTransferSyncReportList(example);
                                if (CollectionUtils.isEmpty(list)) {
                                    // 添加新记录
                                    report.setUserType(userType);
                                    report.setAppletDate(requestDate);
                                    report.setCid(customer.getCid());
                                    report.setApiCode(apiCode);
                                    report.setShortName(customer.getShortName());
                                    report.setCreateTime(new Date());
                                    report.setUpdateTime(new Date());
                                    transferSyncReportMapper.insertSelective(report);
                                } else {
                                    // 更新历史记录
                                    TransferSyncReport transferSyncReport = list.get(0);
                                    if (appletBeginTime.equals(transferSyncReport.getAppletBeginTime())) {
                                        report.setAppletBeginTime(null);
                                    }
                                    if (appletEndTime.equals(transferSyncReport.getAppletEndTime())) {
                                        report.setAppletEndTime(null);
                                    }
                                    if (dataCount.equals(transferSyncReport.getDataCount())) {
                                        report.setDataCount(null);
                                    }
                                    if (report.getAppletBeginTime() != null || report.getAppletEndTime() != null
                                            || report.getDataCount() != null) {
                                        report.setId(transferSyncReport.getId());
                                        report.setUpdateTime(new Date());
                                        transferSyncReportMapper.updateByPrimaryKeySelective(report);
                                    }
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        });
                    }

                }
            }
        }
        threadPool.shutdown();
        while (true) {
            if (threadPool.isTerminated()) {
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (Exception e) {

            }
        }
        log.warn("转化记录-同步记录操作执行完成，耗时{}s", (System.currentTimeMillis() - l) / 1000);
    }

    /**
     * 2022/6/29 17:46
     * 获取apiCode的场景
     * key:apiCode
     * value:userType set
     *
     * @param apiCode apicode
     */
    @SuppressWarnings("all")
    private Map<String, Set<String>> getUserTypeMapByApiCode(String apiCode) {
        VariableDicExample dic = new VariableDicExample();
        VariableDicExample.Criteria criteria = dic.createCriteria().andIsDelEqualTo(1);
        if (StringUtils.isNotBlank(apiCode)) {
            criteria.andApiCodeEqualTo(apiCode);
        }
        List<VariableDic> dicList = variableDicMapper.selectByExample(dic);
        return dicList.parallelStream().collect(Collectors.groupingBy(VariableDic::getApiCode
                , Collectors.mapping(VariableDic::getFieldValue, Collectors.toSet())));
    }

    @Override
    public void reportProcess(Set<String> dateStrSet) {
        reportProcess(dateStrSet, 1, Collections.singletonList(0), null);
    }

    @Override
    public List<TransferSyncReport> findTransferSyncReportList(TransferSyncReportExample example) {
        return transferSyncReportMapper.selectByExample(example);
    }

    @Override
    public PageResultReturn getTransferSyncReportList(int current, int size, String cidOrName, String appletTimeStart
            , String appletTimeEnd, String apiCodes, String userTypes) {
        Map<String, Object> params = queryParams(cidOrName, appletTimeStart, appletTimeEnd, apiCodes, userTypes);
        PageHelper.startPage(current, size);
        List<TransferSyncReportVO> list = transferSyncReportMapper.selectList(params);
        return PageResultReturn.setPageResult(list, current, size);
    }

    @Override
    public Map<String, String> getTransferSyncReportListTotal(String cidOrName, String appletTimeStart
            , String appletTimeEnd, String apiCodes, String userTypes) {
        Map<String, Object> params = queryParams(cidOrName, appletTimeStart, appletTimeEnd, apiCodes, userTypes);
        Map<String, String> map = new HashMap<>(2);
        List<TransferSyncReportNumVO> totalList = transferSyncReportMapper.getReportListTotaltiflash_(params);
        map.put("numTotal", totalList.stream().collect(Collectors.summingLong(TransferSyncReportNumVO::getNumTotal)).toString());
        return map;
    }

    /**
     * 2022/6/30 22:07
     * 组装参数
     */
    private Map<String, Object> queryParams(String cidOrName, String appletTimeStart
            , String appletTimeEnd, String apiCodes, String userTypes) {
        if (StringUtils.isNotEmpty(appletTimeEnd)) {
            appletTimeEnd = LocalDate.parse(appletTimeEnd, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (StringUtils.isNotEmpty(cidOrName) && cidOrName.contains("_")) {
            cidOrName = cidOrName.replace("_", "\\_");
        }
        Map<String, Object> params = new HashMap<>(8);
        params.put("cidOrName", cidOrName);
        params.put("appletTimeEnd", appletTimeEnd);
        params.put("appletTimeStart", appletTimeStart);
        if (StringUtils.isNotBlank(apiCodes)) {
            String[] split = apiCodes.split(",");
            params.put("apiCodeList", Arrays.asList(split));
        }
        if (StringUtils.isNotBlank(userTypes)) {
            String[] split = userTypes.split(",");
            params.put("userTypeList", Arrays.asList(split));
        }
        return params;
    }

    @Override
    public Result<Boolean> nearRealtimeDataCountFragmentsStatis(String dataCountFragmentsMgs) {
        Result<Boolean> result = new Result<>();
        result.setDate(false);
        result.setCode(ResultCode.SUCCESS.getValue());
        boolean statisSwitch = !marketingCommonConfig.getUploadAndTransferDataRealtimeStatisSwitch();
        if (StringUtils.isBlank(dataCountFragmentsMgs) || statisSwitch) {
            return result;
        }
        ApiDataInfoDTO<UserTypeCollectionDTO> apiDataInfoDTO = JSONObject.parseObject(dataCountFragmentsMgs
                , new TypeReference<ApiDataInfoDTO<UserTypeCollectionDTO>>() {
                }.getType());
        String apiCode = apiDataInfoDTO.getApiCode();
        if (StringUtils.isBlank(apiCode)) {
            log.error("转化未获取到apiCode，消息内容：{}", dataCountFragmentsMgs);
            return result;
        }
        MarketingCustomer customer = marketingCustomerService.getCacheCustomerByApiCode(apiCode);
        String cId = StringUtils.isNotBlank(apiDataInfoDTO.getCid()) ? apiDataInfoDTO.getCid() : customer.getCid();
        if (StringUtils.isBlank(cId)) {
            log.error("转化未获取到cid，消息内容：{}", dataCountFragmentsMgs);
            return result;
        }
        LocalDateTime rawDataSaveTime = LocalDateTime.parse(apiDataInfoDTO.getRawDataSaveTimeStr()
                , DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String requestDateStr = rawDataSaveTime.toLocalDate().toString();
        String yyyymmdd = rawDataSaveTime.format(DateTimeFormatter.BASIC_ISO_DATE);
        StringBuilder redisKey = new StringBuilder(RedisKeyConstant.ASYNC_COUNT);
        redisKey.append(cId).append(":").append(apiCode).append(":").append(yyyymmdd).append(":")
                .append(apiDataInfoDTO.getMsgSource()).append(":");
        if (apiDataInfoDTO.transferMsgSource()) {
            String requestId = apiDataInfoDTO.getRequestId();
            Set<String> userTypeSet = CollectionUtils.isEmpty(apiDataInfoDTO.getArgList()) ? null
                    : apiDataInfoDTO.getArgList().stream().map(UserTypeCollectionDTO::getUserType).collect(Collectors.toSet());
            List<TransferSyncReport> syncUserList = marketingTransferSyncUserMapper.selectTransferSyncReportByRequestIdCount(
                    apiCode, cId.contains("-") ? cId.replace("-", "") : cId, requestId, userTypeSet
                    , requestDateStr);
            if (CollectionUtils.isEmpty(syncUserList)) {
                return result;
            }
            TransactionStatus transaction = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
            List<String> hashKeys = new ArrayList<>();
            try {
                for (TransferSyncReport transferSyncReport : syncUserList) {
                    String userType = transferSyncReport.getUserType();
                    String hKey = redisKey + userType;
                    hashKeys.add(hKey);
                    String lockKey = hKey + ":lock";
                    String lockValue = apiDataInfoDTO.getRawDataSaveTimeStr() + transferSyncReport.getId();
                    transferSyncReport.setId(null);
                    try {
                        redisChgService.lock(lockKey, lockValue);
                        // 上锁
                        Map<String, Object> cacheMap = redisChgService.hgetall(hKey);
                        Map<String, String> jsonObject = null;
                        boolean cacheBool = CollectionUtils.isEmpty(cacheMap);
                        if (cacheBool) {
                            // 缓存不存在
                            TransferSyncReportExample example = new TransferSyncReportExample();
                            example.createCriteria().andApiCodeEqualTo(apiCode).andCidEqualTo(cId)
                                    .andUserTypeEqualTo(userType).andAppletDateEqualTo(requestDateStr);
                            List<TransferSyncReport> syncReports = transferSyncReportMapper.selectNumberByExample(example);
                            if (CollectionUtils.isEmpty(syncReports)) {
                                // 未持久化
                                transferSyncReport.setApiCode(apiCode);
                                transferSyncReport.setCid(cId);
                                transferSyncReport.setCreateTime(new Date());
                                transferSyncReport.setUpdateTime(transferSyncReport.getCreateTime());
                                transferSyncReport.setShortName(customer == null ? "" : customer.getShortName());
                                transferSyncReport.setAppletDate(requestDateStr);
                                int i = transferSyncReportMapper.insertSelective(transferSyncReport);
                                if (i > 0 && transferSyncReport.getId() != null) {
                                    TransferSyncReport newTransferReport = new TransferSyncReport();
                                    newTransferReport.setAppletBeginTime(transferSyncReport.getAppletBeginTime());
                                    newTransferReport.setId(transferSyncReport.getId());
                                    newTransferReport.setAppletEndTime(transferSyncReport.getAppletEndTime());
                                    newTransferReport.setDataCount(transferSyncReport.getDataCount());
                                    redisChgService.hmset(hKey, JSONObject.parseObject(JSON.toJSONString(newTransferReport)
                                            , new TypeReference<Map<String, String>>() {
                                            }));
                                    redisChgService.unlock(lockKey, lockValue);
                                    redisChgService.expire(hKey, RandomUtils.nextInt(300, 1800));
                                    continue;
                                }
                            } else {
                                // 已持久化
                                TransferSyncReport syncReportOld = syncReports.get(0);
                                jsonObject = transferSyncReportSummary(transferSyncReport, syncReportOld, false);
                            }
                        } else {
                            // 缓存
                            TransferSyncReport cacheSyncReport = JSONObject.parseObject(JSON.toJSONString(cacheMap)
                                    , new TypeReference<TransferSyncReport>() {
                                    });
                            jsonObject = transferSyncReportSummary(transferSyncReport, cacheSyncReport, true);
                        }
                        int b = transferSyncReportMapper.updateByPrimaryKeySelective(transferSyncReport);
                        if (b > 0 && jsonObject != null) {
                            redisChgService.hmset(hKey, jsonObject);
                            if (cacheBool) {
                                redisChgService.expire(hKey, RandomUtils.nextInt(1800, 3600));
                            }
                        } else {
                            redisChgService.del(hKey);
                        }
                    } finally {
                        redisChgService.unlock(lockKey, lockValue);
                    }
                }
                platformTransactionManager.commit(transaction);
            } catch (Exception e) {
                log.error(e.getMessage() + "\n" + dataCountFragmentsMgs, e);
                platformTransactionManager.rollback(transaction);
                delTransferSyncReportHashKey(hashKeys);
                result.setCode(ResultCode.FAIL.getValue());
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException interruptedException) {
                    log.warn(interruptedException.getMessage(), interruptedException);
                    Thread.currentThread().interrupt();
                }
            }
        }
        return result;
    }

    /**
     * 2024-03-21 17:03
     * 批量删除hash key
     *
     * @param hashTransferSyncReportKeys key
     */
    private void delTransferSyncReportHashKey(List<String> hashTransferSyncReportKeys) {
        String[] keys = hashTransferSyncReportKeys.toArray(new String[0]);
        try {
            long count = redisChgService.del(keys);
            if (count != keys.length) {
                log.warn("转化数据统计清理redis主键部分失败，共:{}；删除:{}；keys:{}"
                        , keys.length, count, Arrays.toString(keys));
                hashTransferSyncReportKeys.forEach((String key) -> {
                    try {
                        redisChgService.del(key);
                    } catch (Exception exception) {
                        log.warn(exception.getMessage(), exception);
                    }
                });
            }
        } catch (Exception exception) {
            log.error(exception + "\n转化数据统计清理redis主键失败:" + Arrays.toString(keys), exception);
        }
    }

    /**
     * 2024-03-12 15:01
     * 汇总数据
     *
     * @param syncReport    目标记录
     * @param syncReportOld 历史记录
     */
    private Map<String, String> transferSyncReportSummary(TransferSyncReport syncReport, TransferSyncReport syncReportOld
            , boolean cacheBool) {
        syncReport.setUserType(null);
        syncReport.setCid(null);
        syncReport.setAppletDate(null);
        syncReport.setApiCode(null);
        syncReport.setCreateTime(null);
        syncReport.setShortName(null);
        syncReport.setRemark(null);
        syncReport.setDataCount(syncReportOld.getDataCount() + syncReport.getDataCount());
        boolean beginBool = (syncReportOld.getAppletBeginTime().before(syncReport.getAppletBeginTime())
                || syncReportOld.getAppletBeginTime().equals(syncReport.getAppletBeginTime()));
        boolean endBool = (syncReportOld.getAppletEndTime().after(syncReport.getAppletEndTime())
                || syncReportOld.getAppletEndTime().equals(syncReport.getAppletEndTime()));
        String cacheString;
        if (cacheBool) {
            syncReport.setAppletBeginTime(beginBool ? null : syncReport.getAppletBeginTime());
            syncReport.setAppletEndTime(endBool ? null : syncReport.getAppletEndTime());
            cacheString = JSON.toJSONString(syncReport);
            syncReport.setId(syncReportOld.getId());
        } else {
            syncReport.setAppletBeginTime(beginBool ? syncReportOld.getAppletBeginTime() : syncReport.getAppletBeginTime());
            syncReport.setAppletEndTime(endBool ? syncReportOld.getAppletEndTime() : syncReport.getAppletEndTime());
            syncReport.setId(syncReportOld.getId());
            cacheString = JSON.toJSONString(syncReport);
            if (endBool) {
                syncReport.setAppletEndTime(null);
            }
            if (beginBool) {
                syncReport.setAppletBeginTime(null);
            }
        }
        Map<String, String> stringMap = JSONObject.parseObject(cacheString, new TypeReference<Map<String, String>>() {
        });
        syncReport.setUpdateTime(new Date());
        return stringMap;
    }
}
