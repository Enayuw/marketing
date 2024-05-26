package com.br.marketing.monkeydata.handle.yixin;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.bo.ZaMarketDataBO;
import com.br.marketing.bo.ZhonganRosterLockingDataBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.qifu.BizData;
import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.enums.YxTransferFilterEnum;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.ZhonganMarketingBanMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.monkeydata.query.ZhongAnCellZkDateQuery;
import com.br.marketing.monkeydata.query.ZhongAnMobileMd5BizDateQuery;
import com.br.marketing.monkeydata.service.Impl.DistributeSoleProcessor;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 宜信转化过滤推送百应
 */
@Service
@Slf4j
public class YixinTransferPushToBaiYingHandler extends IMonkeyDataHandle<MarketingTransferSyncUser
        , MarketingTransferSyncUser, Page2Condition<MarketingTransferSyncUser>> {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

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

    private final static String TITLE = "【宜信转化过滤推送百应】";

    @Override
    public Result<IterationResult<MarketingTransferSyncUser, Page2Condition<MarketingTransferSyncUser>>> getInputData(
            Page2Condition<MarketingTransferSyncUser> condition) {
        return null;
    }

    @Override
    public Result<?> customizedAction(Page2Condition<MarketingTransferSyncUser> condition) {
        Result<?> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());

        ThreadPoolExecutor processPool = BrExecutors.getThreadPool(2, 2, 10);
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(24, 24, new SynchronousQueue<>());

        MarketingTransferSyncUser conditionParam = condition.getParam();

        List<Future<Result<List<MarketingTransferSyncUser>>>> futureList = new ArrayList<>();
        Integer pageSize = condition.getPageSize();
        String apiCode = conditionParam.getApiCode();
        String requestData = conditionParam.getRequestData();
        String tCid = tableCreateService.getTcId(apiCode);

        Long indexId = null;
        while(true) {
            // 循环获取条件数据，每次pageSize条
            final List<MarketingTransferSyncUser> pageList = marketingTransferSyncUserMapper.getYxTransferByRequestDate(
                    tCid, apiCode, requestData, indexId, pageSize);

            if (CollectionUtils.isEmpty(pageList)) {
                break;
            }

            indexId = pageList.get(pageList.size() - 1).getId();

            setThreadPoolParam(processPool, pushPool);

            // 根据规则分类，推送数据
            futureList.add(processPool.submit(() -> processData(pageList, conditionParam, pushPool)));
        }

        for (Future<Result<List<MarketingTransferSyncUser>>> future : futureList) {
            try {
                future.get(1, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage()
                        , TITLE), e);
//                future.cancel(true);
                result.setCode(ResultCode.FAIL.getValue());
            }
        }

        long taskCount = -1;
        processPool.shutdown();
        try {
            while (!processPool.awaitTermination(30, TimeUnit.SECONDS)) {
                long completedTask2Count = processPool.getCompletedTaskCount();
                if (taskCount == completedTask2Count) {
                    result.setCode(ResultCode.FAIL.getValue());
                    log.warn(TITLE+"业务线程等待超时, {}, {}", apiCode, requestData);
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage()
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
                    result.setCode(ResultCode.FAIL.getValue());
                    log.warn(TITLE+"推送线程等待超时, {}, {}", apiCode, requestData);
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage()
                    , TITLE), e);
            result.setCode(ResultCode.FAIL.getValue());
            Thread.currentThread().interrupt();
        }
        return result;
    }

    @Override
    public Result<List<MarketingTransferSyncUser>> processData(List<MarketingTransferSyncUser> inList) {
        return null;
    }

    public Result<List<MarketingTransferSyncUser>> processData(List<MarketingTransferSyncUser> pageList,
                MarketingTransferSyncUser pageParam, ThreadPoolExecutor pushPool) {
        Result<List<MarketingTransferSyncUser>> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());

        // pageParam
        String apiCode = pageParam.getApiCode();
        String requestData = pageParam.getRequestData();

        try {
            List<MarketingTransferSyncUser> pushList = new ArrayList<>();
            Set<String> filterSet = YxTransferFilterEnum.getFilterSetOrderByPriority();
            for(String filterName : filterSet){
                YxTransferFilter filter = SpringUtil.getBean(filterName, YxTransferFilter.class);
                List filteredList = filter.filter(pageList);
                if(CollectionUtils.isEmpty(filteredList)){
                    continue;
                }
            }
            // distribute去重 custNum + distribute_date
            // distributeIds = distributeSoleProcessor.process(pushList, pageParam);

            Result<?> resultAction = resultAction(pushList, pushPool);
            result.setCode(resultAction.getCode());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Result<?> resultAction(List<MarketingTransferSyncUser> outputDataList) {
        return null;
    }

    public Result<?> resultAction(List<MarketingTransferSyncUser> outputDataList, ThreadPoolExecutor pushPool) {
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
     * 配置线程池参数
     */
    private void setThreadPoolParam(ThreadPoolExecutor processPool, ThreadPoolExecutor pushPool) {
        Map<String, Integer> threadPoolConfig = marketingCommonConfig.getYiXinTransferPushBaiYingThreadPool();
        int processPoolSize = threadPoolConfig.get("processPool");
        int pushPoolSize = threadPoolConfig.get("pushPoolSize");

        if (ObjectUtils.isEmpty(processPoolSize) || processPoolSize < 1) {
            processPoolSize = Runtime.getRuntime().availableProcessors() * 10;
        }
        if (ObjectUtils.isEmpty(pushPoolSize) || pushPoolSize < 1) {
            pushPoolSize = Runtime.getRuntime().availableProcessors() * 10;
        }

        processPool.setCorePoolSize(processPoolSize);
        processPool.setMaximumPoolSize(processPoolSize);

        pushPool.setCorePoolSize(pushPoolSize);
        pushPool.setMaximumPoolSize(pushPoolSize);
    }


}
