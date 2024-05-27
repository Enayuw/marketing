package com.br.marketing.monkeydata.handle.yixin;

import cn.hutool.extra.spring.SpringUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.baiying.ByApiServiceClient;
import com.br.marketing.client.baiying.input.BlacklistDataDTO;
import com.br.marketing.client.baiying.input.ReqBlacklistDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.YxTransferFilterEnum;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.ZhonganMarketingBanMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.yixin.YiXinCondition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 宜信转化过滤推送百应
 */
@Service
@Slf4j
public class YiXinTransferPushToBaiYingHandler extends IMonkeyDataHandle<MarketingTransferSyncUser
        , MarketingTransferSyncUser, YiXinCondition> {

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
    private YiXinTransferPushBaiYingDistributeSoleProcessor distributeSoleProcessor;

    @Resource
    private ByApiServiceClient byApiServiceClient;

    private final static String TITLE = "【宜信转化过滤推送百应】";

    @Override
    public Result<IterationResult<MarketingTransferSyncUser, YiXinCondition>> getInputData(
            YiXinCondition condition) {
        return null;
    }

    @Override
    public Result<?> customizedAction(YiXinCondition condition) {
        Result<?> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());

        ThreadPoolExecutor processPool = BrExecutors.getThreadPool(2, 2, 10);
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(24, 24, new SynchronousQueue<>());

        List<Future<Result<List<MarketingTransferSyncUser>>>> futureList = new ArrayList<>();
        Integer pageSize = condition.getPageSize();
        String apiCode = condition.getApiCode();
        String requestData = condition.getRequestData();
        String synApiCode = condition.getSynApiCode();
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
            futureList.add(processPool.submit(() -> processData(pageList, condition, pushPool)));
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
                YiXinCondition condition, ThreadPoolExecutor pushPool) {
        Result<List<MarketingTransferSyncUser>> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());

        // pageParam
        String apiCode = condition.getApiCode();
        String requestData = condition.getRequestData();
        String synApiCode = condition.getSynApiCode();

        Set<String> custNumSets = pageList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        Map<String, SyncUserValidityPeriodsBO> cellToSyncUserBoMap = transferDataValidityPeriodService
                .getValidityPeriodsByCustNum(custNumSets, apiCode, requestData);

        // 未获取到上传数据
        if (CollectionUtils.isEmpty(cellToSyncUserBoMap)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_VALIDITY_PERIOD.getCode(),
                    "apiCode:" + apiCode + ", bizDate:" + requestData + "未获取到上传数据或未配置有效期！",
                    "宜信转化过滤推送百应"));
            return result;
        }

        try {
            List<MarketingTransferSyncUser> pushList = new ArrayList<>();
            List<String> filterList = YxTransferFilterEnum.getFilterListOrderByPriority();
            for(String filterName : filterList){
                YxTransferFilter filter = SpringUtil.getBean(filterName, YxTransferFilter.class);
                List filteredList = filter.filter(pageList);
                if(CollectionUtils.isEmpty(filteredList)){
                    continue;
                }
                pushList.addAll(filteredList);
            }

            // distribute去重 custNum + distribute_date
            distributeSoleProcessor.process(pushList, condition);

            Result<?> resultAction = resultAction(pushList, condition, pushPool);
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

    public Result<?> resultAction(List<MarketingTransferSyncUser> outputDataList, YiXinCondition condition, ThreadPoolExecutor pushPool) {
        Result<Object> result = new Result<>();
        if (CollectionUtils.isEmpty(outputDataList)) {
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }

        Map<String, Object> pushConfigMap = marketingCommonConfig.getYiXinTransferPushBaiYingPush();
        int pushSize = pushConfigMap.get("pushPartSize") != null ?
                Integer.parseInt(String.valueOf(pushConfigMap.get("pushPartSize"))) : 500;
        String pushMethod = pushConfigMap.get("pushMethod") != null ?
                String.valueOf(pushConfigMap.get("pushMethod")) : "blackData";

        int size = outputDataList.size();
        int count = 0;
        List<BlacklistDataDTO> pushList = new ArrayList<>();

        for (MarketingTransferSyncUser transferSyncUser : outputDataList) {
            BlacklistDataDTO blacklistDataDTO = new BlacklistDataDTO();
            blacklistDataDTO.setCaseNum(transferSyncUser.getCustNum());
            pushList.add(blacklistDataDTO);
            count++;

            if (pushList.size() == pushSize || size == count) {
                List<BlacklistDataDTO> finalList = pushList;
                pushPool.execute(() -> {
                    ReqBlacklistDTO reqBlacklistDTO = new ReqBlacklistDTO();
                    reqBlacklistDTO.setMethod(pushMethod);
                    reqBlacklistDTO.setApiCode(condition.getSynApiCode());
                    reqBlacklistDTO.setData(finalList);
                    byApiServiceClient.pushBaiying(reqBlacklistDTO);
                });
                pushList = new ArrayList<>();
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
