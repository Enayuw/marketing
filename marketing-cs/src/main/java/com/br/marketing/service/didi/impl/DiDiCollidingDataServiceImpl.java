package com.br.marketing.service.didi.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.didi.DiDiV5Client;
import com.br.marketing.client.didi.input.v5.DiDiV5CollidingRequestDTO;
import com.br.marketing.client.didi.output.v5.DiDiV5CollidingResultResponseDTO;
import com.br.marketing.client.didi.utils.MD5Util;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.DiDiDataLoopCycle;
import com.br.marketing.entity.DiDiV5CollidingData;
import com.br.marketing.entity.DiDiV5CollidingDataLog;
import com.br.marketing.mapper.*;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.service.didi.DiDiCollidingDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DiDiCollidingDataServiceImpl implements DiDiCollidingDataService {

    private final static String TITLE = "【滴滴V5-撞库任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DiDiV5CollidingDataMapper diDiV5CollidingDataMapper;

    @Resource
    private DiDiV5CollidingDataLogMapper diDiV5CollidingDataLogMapper;

    @Resource
    private DidiCallbackDataLogMapper didiCallbackDataLogMapper;

    @Resource
    private DiDiV5Client diDiV5Client;

    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Resource
    private GeneralDataCleanService generalDataCleanService;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private DiDiV5DataLoopCycleMapper diDiV5DataLoopCycleMapper;

    @Resource
    private DiDiV5CollidingDataRobMapper diDiV5CollidingDataRobMapper;


    @Override
    public void colliding(JobExecutionMultipleShardingContext context) {
        // 获取分片信息
        List<Integer> shardingItems = context.getShardingItems();
        int shardingTotalCount = context.getShardingTotalCount();
        log.warn("滴滴短信流量数据撞库任务开始执行，总分片数：{}，当前分片：{}", shardingTotalCount, shardingItems);

        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.DIDI_V5_COLLIDING.getName(), 50, 50);
        List<Long> fileIds = diDiV5CollidingDataMapper.queryCollidingFileIds(DateUtil.beginOfDay(new Date()), new Date());
        if (CollectionUtils.isEmpty(fileIds)) {
            log.warn("滴滴短信流量数据撞库任务，分片{}未查询到待处理文件", shardingItems);
            return;
        }
        localFileMapper.updateUploadStartTimeById(fileIds, new Date());
        JSONObject collidingConfig = marketingCommonConfig.getDiDiV5Config();
        int limit = collidingConfig.getInteger("limit") != null ? collidingConfig.getInteger("limit") : 2000;
        String mediaName = collidingConfig.getString("mediaName") != null ? collidingConfig.getString("mediaName") : "bairongC";
        String token = collidingConfig.getString("token") != null ? collidingConfig.getString("token") : "9Hqeoi36CJfdA7n4";
        boolean preScreen1 = collidingConfig.getBoolean("preScreen1") == null || collidingConfig.getBoolean("preScreen1");
        boolean preScreen2 = collidingConfig.getBoolean("preScreen2") == null || collidingConfig.getBoolean("preScreen2");
        // 收集所有异步任务的Future，用于等待所有任务完成
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        while (true) {
            // 使用分片查询，只处理属于当前分片的数据
            List<DiDiV5CollidingData> dataList = diDiV5CollidingDataMapper.queryCollidingDataBySharding(
                    limit, DateUtil.beginOfDay(new Date()), new Date(), shardingTotalCount, shardingItems);
            if (CollectionUtils.isEmpty(dataList)) {
                break;
            }
            markAsPushing(dataList);
            List<String> allCellsToCheck = dataList.stream().map(DiDiV5CollidingData::getCell).distinct().collect(Collectors.toList());
            Set<String> existingCellsInPreScreen1 = preScreen1 ?
                    new HashSet<>(didiCallbackDataLogMapper.checkCellBatch(allCellsToCheck)) : Collections.emptySet();
            Set<String> existingCellsInPreScreen2 = preScreen2 ?
                    new HashSet<>(diDiV5CollidingDataLogMapper.checkCellBatch(allCellsToCheck)) : Collections.emptySet();
            dataList.forEach((DiDiV5CollidingData data) -> {
                String currentCell = data.getCell();
                boolean existsInScreen1 = preScreen1 && existingCellsInPreScreen1.contains(currentCell);
                boolean existsInScreen2 = preScreen2 && existingCellsInPreScreen2.contains(currentCell);
                if ((!preScreen1 || !existsInScreen1) && (!preScreen2 || !existsInScreen2)) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(
                            () -> collidingData(data, mediaName, token),
                            pushPool
                    );
                    futures.add(future);
                }
            });
        }
        log.warn("分片{}等待所有撞库任务完成，共{}个任务", shardingItems, futures.size());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // 所有任务完成后，再更新本地文件状态
        updateLocalFiles(fileIds);
        pushPool.shutdownAndAwaitTermination();
    }

    private void updateLocalFiles(List<Long> fileIds) {
        for (Long fileId : fileIds) {
            int count = diDiV5CollidingDataMapper.getPushStatusCountByLocalId(fileId, 0, DateUtil.beginOfDay(new Date()),
                    DateUtil.endOfDay(new Date()));
            if (count > 0) {
                continue;
            }
            int successCount = diDiV5CollidingDataMapper.getPushStatusCountByLocalId(fileId, 3, DateUtil.beginOfDay(new Date()),
                    DateUtil.endOfDay(new Date()));
            localFileMapper.updatePushEndTimeById(fileId, successCount, new Date());
        }
    }

    /**
     * 标记为正在处理状态
     *
     * @param dataList 数据列表
     * @author senyang.zheng
     * @since 2025/12/19
     */
    private void markAsPushing(List<DiDiV5CollidingData> dataList) {
        List<Long> ids = dataList.stream().map(DiDiV5CollidingData::getId).toList();
        diDiV5CollidingDataMapper.updatePushStatusByIds(1, ids);
    }


    private void collidingData(DiDiV5CollidingData data, String mediaName, String token) {
        //单个撞库异常不影响其他撞库
        try {
            long start = System.currentTimeMillis();
            Result<String> response = diDiV5Client.colliding(mediaName, buildRequest(data.getCell(), token));
            String resData = response.getData();
            JSONObject resJson = JSONObject.parseObject(resData);
            String httpcode = resJson.getString("httpcode");
            String content = resJson.getString("content");
            boolean success = "200".equals(httpcode) || StringUtils.isNotBlank(content);
            data.setPushStatus(success ? 3 : 2);
            data.setUpdateTime(new Date());
            diDiV5CollidingDataMapper.updateByPrimaryKey(data);
            pushToMq(data, httpcode, content);
            log.warn("滴滴短信流量数据撞库任务，单线程耗时：{}ms", (System.currentTimeMillis() - start));
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "该手机号撞库异常：" + data.getCell() + "id:" + data.getId()), e);
        }
    }

    private void pushToMq(DiDiV5CollidingData data, String httpcode, String content) {
        JSONObject mqJson = new JSONObject();
        DiDiV5CollidingDataLog diDiV5CollidingDataLog = new DiDiV5CollidingDataLog();
        diDiV5CollidingDataLog.setApiCode(data.getApiCode());
        diDiV5CollidingDataLog.setCell(data.getCell());
        diDiV5CollidingDataLog.setHttpCode(httpcode);
        diDiV5CollidingDataLog.setDataId(data.getId());
        diDiV5CollidingDataLog.setLocalId(data.getLocalId());
        diDiV5CollidingDataLog.setReturnContent(content);
        if (StringUtils.isNotBlank(content)) {
            DiDiV5CollidingResultResponseDTO diDiV5CollidingResultResponseDTO = JSON.parseObject(content,
                    DiDiV5CollidingResultResponseDTO.class);
            diDiV5CollidingDataLog.setErrorCode(diDiV5CollidingResultResponseDTO.getErrorCode());
            diDiV5CollidingDataLog.setErrorMessage(diDiV5CollidingResultResponseDTO.getErrorMessage());
            diDiV5CollidingDataLog.setResult(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getResult()));
            diDiV5CollidingDataLog.setFailReason(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getFailReason()));
            diDiV5CollidingDataLog.setUserGroup(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getUserGroup()));
            diDiV5CollidingDataLog.setNextTime(String.valueOf(diDiV5CollidingResultResponseDTO.getData().getNextTime()));
            mqJson.put("diDiV5CollidingResultResponseDTO", diDiV5CollidingResultResponseDTO);
        }
        mqJson.put("diDiV5CollidingDataLog", diDiV5CollidingDataLog);
        rocketMqSwitch.syncSend(MarketingOutsideInterfaceConstants.TOPIC, MarketingOutsideInterfaceConstants.TAG_MARKETING_DIDI_V5_COLLIDING_DATA,
                mqJson.toJSONString());
    }

    private DiDiV5CollidingRequestDTO buildRequest(String cell, String token) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return new DiDiV5CollidingRequestDTO().setSign(cell).setTimestamp(timestamp).setSignature(MD5Util.encode(cell + timestamp + token));
    }

    @Override
    public Result<Boolean> saveDiDiCollidingDataLog(String bodyString) {
        log.warn(TITLE + "，开始");
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(false);
        JSONObject didiConfig = marketingCommonConfig.getDiDiV5Config();
        List<String> retryHttpCode = didiConfig.getJSONArray("retryHttpCode").toJavaList(String.class);
        try {
            JSONObject dto = JSONObject.parseObject(bodyString);
            String responseStr = dto.getString("diDiV5CollidingResultResponseDTO");
            DiDiV5CollidingResultResponseDTO diDiV5CollidingResultResponseDTO = JSONObject.parseObject(responseStr,
                    DiDiV5CollidingResultResponseDTO.class);
            String dataLogStr = dto.getString("diDiV5CollidingDataLog");
            DiDiV5CollidingDataLog dataLog = JSONObject.parseObject(dataLogStr, DiDiV5CollidingDataLog.class);
            diDiV5CollidingDataLogMapper.insertSelective(dataLog);
            boolean isRob = "F".equals(dataLog.getSourceType());

            if ("false".equalsIgnoreCase(dataLog.getResult()) && "1".equalsIgnoreCase(dataLog.getFailReason())) {
                JSONObject collidingConfig = marketingCommonConfig.getDiDiV5Config();
                Long retrieveFileId = collidingConfig.getLong("retrieveFileId");

                DiDiDataLoopCycle diDiDataLoopCycle = new DiDiDataLoopCycle();
                BeanUtils.copyProperties(dataLog, diDiDataLoopCycle);
                diDiDataLoopCycle.setPackageId(String.valueOf(dataLog.getLocalId()));
                diDiDataLoopCycle.setSourceType("T");
                diDiDataLoopCycle.setLockType(2);
                diDiDataLoopCycle.setPackageId(retrieveFileId.toString());
                diDiDataLoopCycle.setCollidingTime(new Date(Long.parseLong(dataLog.getNextTime())));
                diDiDataLoopCycle.setCreateTime(new Date());
                diDiDataLoopCycle.setUpdateTime(new Date());
                diDiV5DataLoopCycleMapper.insertSelective(diDiDataLoopCycle);
            } else if (diDiV5CollidingResultResponseDTO.getData().getResult()) {
                cleanAndUpload(diDiV5CollidingResultResponseDTO, dataLog);
            }
            if (!retryHttpCode.contains(dataLog.getHttpCode())) {
                if (isRob) {
                    diDiV5CollidingDataRobMapper.updatePushTimeByIds(new Date(), Lists.newArrayList(dataLog.getDataId()));
                } else {
                    diDiV5DataLoopCycleMapper.updatePushTimeByIds(new Date(), Lists.newArrayList(dataLog.getDataId()));
                }
            }
        } catch (Exception ex) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), "数清据洗/上传失败,bodyString:" + bodyString,
                    TITLE), ex);
        }
        return result;
    }

    private Result<Boolean> cleanAndUpload(DiDiV5CollidingResultResponseDTO responseDTO, DiDiV5CollidingDataLog dataLog) throws NoSuchFieldException {
        String userType = "1";
        // 数据包装
        JSONObject cleanJson = (JSONObject) JSONObject.toJSON(responseDTO.getData());
        cleanJson.put("cell", dataLog.getCell());
        cleanJson.put("userGroup", dataLog.getUserGroup());
        cleanJson.put("userType", userType);
        Result cleanResult = generalDataCleanService.uploadClean(
                Lists.newArrayList(cleanJson),
                dataLog.getApiCode());

        if (cleanResult == null || !cleanResult.isSuccess()) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), "数据清洗失败,dataId:" + dataLog.getDataId(), TITLE));
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(false);
        }
        // 生成批次号与请求号
        String batchNo = dataLog.getApiCode() + "_" + DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_FORMAT);
        String requestId = batchNo + "_" + RandomStringUtils.randomAlphabetic(16) + UUID.randomUUID();
        // 转换清洗结果
        List<MarketingPreUserDetailDTO> userList = (List<MarketingPreUserDetailDTO>) cleanResult.getData();
        UploadDataDTO uploadDataDTO = initUploadData(dataLog.getApiCode(), batchNo, userList, requestId);
        return pushInfoService.pushUploadByRetry(uploadDataDTO, null);
    }


    /**
     * 封装异步调用上传的数据
     *
     * @param apiCode   apiCode
     * @param syncUsers 具体数据对象
     */
    private UploadDataDTO initUploadData(String apiCode, String batchNo, List<MarketingPreUserDetailDTO> syncUsers, String requestId) {
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(batchNo);
        marketingPreUserDTO.setRequestId(requestId);
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }
}