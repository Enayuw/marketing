package com.br.marketing.monkeydata.handle.yixin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.br.common.log.AlertLog;
import com.br.marketing.client.baiying.ByApiServiceClient;
import com.br.marketing.client.baiying.input.BlacklistDataDTO;
import com.br.marketing.client.baiying.input.ReqBlacklistDTO;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.BlackQueryDetailDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneQueryDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingDataValidConfigExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.yixin.YiXinCondition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.br.marketing.monkeydata.handle.yixin.sole.YiXinBlackPushRedisSoleProcessor;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * 宜信转化过滤推送百应
 */
@Service
@Slf4j
public class YiXinBlackPushToBioclooHandler extends IMonkeyDataHandle<MarketingSyncUser, MarketingSyncUser, YiXinCondition> {

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private YiXinBlackPushRedisSoleProcessor blackPushRedisSoleProcessor;

    @Resource
    private ByApiServiceClient byApiServiceClient;

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    private final static String TITLE = "【宜信转化过滤推送百应】";

    @Override
    public Result<IterationResult<MarketingSyncUser, YiXinCondition>> getInputData(YiXinCondition condition) {
        return null;
    }

    @Override
    public Result<?> customizedAction(YiXinCondition condition) {
        Result<?> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());

        ThreadPoolExecutor processPool = BrExecutors.getThreadPool(12, 12, 20);
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(24, 24, new SynchronousQueue<>());

        List<Future<Result<List<MarketingSyncUser>>>> futureList = new ArrayList<>();
        Integer pageSize = condition.getPageSize();
        String apiCode = condition.getApiCode();
        String requestData = condition.getRequestData();
        String synApiCode = condition.getSynApiCode();

        List<MarketingDataValidConfig> configList = findConfigByBetweenDate(synApiCode, requestData);
        if (CollectionUtils.isEmpty(configList)) {
            log.error(TITLE + "未配置有效期，请检查");
            return result;
        }

        for (MarketingDataValidConfig config : configList) {
            String userType = config.getUserType();
            String appletDate = config.getAppletDate();
            log.warn(TITLE + "当前有效期, {}, {}", userType, appletDate);

            Long indexId = null;
            while (true) {
                // 循环获取条件数据，每次pageSize条
                final List<MarketingSyncUser> pageList =
                    marketingSyncUserMapper.getYiXinNewSyncUserByDateAndResourceChannel(synApiCode, appletDate, "2", userType, pageSize, indexId);
                if (CollectionUtils.isEmpty(pageList)) {
                    break;
                }
                indexId = pageList.get(pageList.size() - 1).getId();

                setThreadPoolParam(processPool, pushPool);

                // 根据规则分类，推送数据
                futureList.add(processPool.submit(() -> processData(pageList, condition, pushPool)));
            }
        }

        for (Future<Result<List<MarketingSyncUser>>> future : futureList) {
            try {
                future.get(1, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage(), TITLE), e);
                // future.cancel(true);
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
                    log.warn(TITLE + "业务线程等待超时, {}, {}", apiCode, requestData);
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage(), TITLE), e);
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
                    log.warn(TITLE + "推送线程等待超时, {}, {}", apiCode, requestData);
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage(), TITLE), e);
            result.setCode(ResultCode.FAIL.getValue());
            Thread.currentThread().interrupt();
        }
        return result;
    }

    @Override
    public Result<List<MarketingSyncUser>> processData(List<MarketingSyncUser> inList) {
        return null;
    }

    public Result<List<MarketingSyncUser>> processData(List<MarketingSyncUser> pageList, YiXinCondition condition, ThreadPoolExecutor pushPool) {
        Result<List<MarketingSyncUser>> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());
        try {
            // pageParam
            String apiCode = condition.getApiCode();

            // queryBlack
            List<MarketingSyncUser> pushList = new ArrayList<>();
            Result<Map<String, String>> queryBlackResult = getBlackList(pageList, apiCode);

            HashMap<String, String> blackData = new HashMap<>();
            if (ResultCode.SUCCESS.getValue().equals(queryBlackResult.getCode())) {
                blackData.putAll(queryBlackResult.getData());
            }

            List<MarketingSyncUser> blackList = pageList.stream().filter(syncUser -> !StringUtils.isBlank(blackData.get(syncUser.getId().toString()))
                && blackData.get(syncUser.getId().toString()).equals("Y")).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(blackList)) {
                return result;
            }
            pushList = blackList;

            // distribute去重 custNum + distribute_date
            blackPushRedisSoleProcessor.process(pushList, condition, DistributeTypeEnum.YIXIN_TRANSFER_PUSH_BIOCLOO.getValue());

            Result<?> resultAction = resultAction(pushList, condition, pushPool);
            result.setCode(resultAction.getCode());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Result<?> resultAction(List<MarketingSyncUser> outputDataList) {
        return null;
    }

    public Result<?> resultAction(List<MarketingSyncUser> outputDataList, YiXinCondition condition, ThreadPoolExecutor pushPool) {
        Result<Object> result = new Result<>();
        if (CollectionUtils.isEmpty(outputDataList)) {
            result.setCode(ResultCode.FAIL.getValue());
            return result;
        }

        Map<String, Object> pushConfigMap = marketingCommonConfig.getYiXinTransferPushBaiYingPush();
        int pushSize = pushConfigMap.get("pushPartSize") != null ? Integer.parseInt(String.valueOf(pushConfigMap.get("pushPartSize"))) : 500;
        String pushMethod = pushConfigMap.get("pushMethod") != null ? String.valueOf(pushConfigMap.get("pushMethod")) : "blackData";
        int size = outputDataList.size();
        int count = 0;
        List<BlacklistDataDTO> pushBioclooList = new ArrayList<>();
        for (MarketingSyncUser syncUser : outputDataList) {
            BlacklistDataDTO bioclooBlackDataList = new BlacklistDataDTO();
            bioclooBlackDataList.setCaseNum(syncUser.getCustNum());
            bioclooBlackDataList.setExpireDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).concat(" 23:59:59"));
            bioclooBlackDataList.setPhone(syncUser.getCellMd5());
            pushBioclooList.add(bioclooBlackDataList);
            count++;
            if (pushBioclooList.size() == pushSize || size == count) {
                List<BlacklistDataDTO> finalList = pushBioclooList;
                pushPool.execute(() -> {
                    // 紧急需求，增加了推送百可录逻辑（后续逻辑变更请注意）
                    ReqBlacklistDTO reqBklBlacklistDTO = new ReqBlacklistDTO();
                    reqBklBlacklistDTO.setMethod(pushMethod);
                    reqBklBlacklistDTO.setApiCode(condition.getSynApiCode());
                    reqBklBlacklistDTO.setData(finalList);
                    byApiServiceClient.pushDataToBiocloo(reqBklBlacklistDTO, 0);
                });
                pushBioclooList = new ArrayList<>();
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
        int processPoolSize = threadPoolConfig.get("processPoolSize");
        int pushPoolSize = threadPoolConfig.get("pushPoolSize");

        if (ObjectUtils.isEmpty(processPoolSize) || processPoolSize < 1) {
            processPoolSize = Runtime.getRuntime().availableProcessors() * 10;
        }
        if (ObjectUtils.isEmpty(pushPoolSize) || pushPoolSize < 1) {
            pushPoolSize = Runtime.getRuntime().availableProcessors() * 10;
        }

        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(processPool, processPoolSize);

        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pushPool, pushPoolSize);
    }

    public Result<Map<String, String>> getBlackList(List<MarketingSyncUser> syncUserList, String apiCode) {
        List<BlackQueryDetailDTO> blackQueryDetailDTOS = new ArrayList<>();
        ReqBlackPhoneQueryDTO dto = new ReqBlackPhoneQueryDTO();
        dto.setApiCode(apiCode);
        dto.setDetailBlackPhoneDTO(blackQueryDetailDTOS);
        syncUserList.forEach((MarketingSyncUser syncUser) -> {
            BlackQueryDetailDTO blackQueryDetailDTO = new BlackQueryDetailDTO();
            blackQueryDetailDTO.setDataId(syncUser.getId().toString());
            blackQueryDetailDTO.setApiCode(apiCode);
            blackQueryDetailDTO.setCaseNum(syncUser.getCustNum());
            blackQueryDetailDTOS.add(blackQueryDetailDTO);
        });
        return robotaiApiServiceClient.queryBlackPhone(dto);
    }

    private List<MarketingDataValidConfig> findConfigByBetweenDate(String synApiCode, String date) {
        MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(synApiCode).andValidStartDateLessThanOrEqualTo(date).andValidEndDateGreaterThanOrEqualTo(date)
            .andIsDelEqualTo(1);
        example.setOrderByClause("create_time desc, update_time desc");
        return marketingDataValidConfigMapper.selectByExample(example);
    }
}
