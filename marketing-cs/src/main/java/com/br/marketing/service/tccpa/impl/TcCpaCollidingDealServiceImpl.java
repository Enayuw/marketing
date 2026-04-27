package com.br.marketing.service.tccpa.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTcyrCpaSuccessData;
import com.br.marketing.entity.MarketingTcyrCpaSuccessFile;
import com.br.marketing.entity.MarketingTcyrCpaSuccessRecord;
import com.br.marketing.entity.TcyrCpaCollectTask;
import com.br.marketing.enums.*;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessDataMapper;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessFileMapper;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessRecordMapper;
import com.br.marketing.mapper.TcyrCpaCollectTaskMapper;
import com.br.marketing.service.tccpa.TcCpaCollidingDealService;
import com.br.marketing.service.tccpa.TcCpaCustCellMappingService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class TcCpaCollidingDealServiceImpl implements TcCpaCollidingDealService {

    private final static String TITLE = "【同程易融CPA-colliding任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcCpaCustCellMappingService custCellMappingService;

    @Resource
    private MarketingTcyrCpaSuccessRecordMapper tcyrCpaSuccessRecordMapper;

    @Resource
    private MarketingTcyrCpaSuccessFileMapper tcyrCpaSuccessFileMapper;

    @Resource
    private MarketingTcyrCpaSuccessDataMapper tcyrCpaSuccessDataMapper;

    @Resource
    private TcyrCpaCollectTaskMapper tcyrCpaCollectTaskMapper;

    @Autowired
    private RedisChgService redisChgService;

    @Override
    public void shardProcess(String apiCode) {
        String lockKey = RedisKeyConstant.tcyrCpaCollidingSuccessDeal.concat(apiCode);
        String lockValue = UUID.randomUUID().toString();
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_CPA_COLLIDING_DEAL.getName(), 50, 50);
        try {
            for (;;) {
                if (!marketingCommonConfig.getTcyrCpaCollidingDealShardConfig().getBoolean("jobSwitch")) {
                    break;
                }
                //1.抢锁
                redisChgService.lockLoop(lockKey, lockValue, 5000L, null);
                //2.查询单条未处理的csvFile
                MarketingTcyrCpaSuccessFile tcyrCpaSuccessFile = tcyrCpaSuccessFileMapper.selectColliDingNoDealSingleFile(apiCode, TcCpaCollidingDealStatusEnum.DEAL_NO.getValue());
                if (ObjectUtil.isEmpty(tcyrCpaSuccessFile)) {
                    redisChgService.unlock(lockKey, lockValue);
                    break;
                }
                //3.修改文件quickDeal处理状态-释放锁
                tcyrCpaSuccessFileMapper.updateColliDingDataDealStatus(tcyrCpaSuccessFile.getId(),TcCpaCollidingDealStatusEnum.DEAL_MIDDLE.getValue());
                redisChgService.unlock(lockKey, lockValue);
                //4.csvFile 快速处理流程
                csvFileDbDeal(tcyrCpaSuccessFile,actionPool);
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }finally {
            //5、异常时释放锁(finally)
            redisChgService.unlock(lockKey, lockValue);
            actionPool.shutdownAndAwaitTermination();
        }
    }

    private void csvFileDbDeal(MarketingTcyrCpaSuccessFile tcyrCpaSuccessFile, TpDynamicExecutor actionPool) {
        long startTime = System.currentTimeMillis();
        log.warn("TITLE:{},sync_file_id:{} db_deal执行", TITLE, tcyrCpaSuccessFile.getId());
        //1.判断文件存在
        File txtFile = new File(tcyrCpaSuccessFile.getFilePath());
        if (!txtFile.exists()) {
            tcyrCpaSuccessFileMapper.updateColliDingDataDealStatus(tcyrCpaSuccessFile.getId(), TcCpaCollidingDealStatusEnum.NO_FILE.getValue());
            return;
        }
        MarketingTcyrCpaSuccessRecord tcyrCpaSuccessRecord = tcyrCpaSuccessRecordMapper.selectByPrimaryKey(tcyrCpaSuccessFile.getSyncRecordId());
        //2.csvFileDbDeal流程
        long totalCount = 0L;
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            List<String> batchData = new ArrayList<>();
            List<CompletableFuture<Void>> futures = Lists.newArrayList();
            while ((line = reader.readLine()) != null) {
                batchData.add(line);
                if (batchData.size() == marketingCommonConfig.getTcyrCpaCollidingDealShardConfig().getInteger("pageSize")) {
                    if (!marketingCommonConfig.getTcyrCpaCollidingDealShardConfig().getBoolean("jobSwitch")) {
                        batchData.clear();
                        break;
                    }
                    List<String> batchDealData = new ArrayList<>(batchData);
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                                    dbDealBatchLine(tcyrCpaSuccessFile.getApiCode(), tcyrCpaSuccessRecord.getBatchNo(),
                                            tcyrCpaSuccessRecord.getData(), tcyrCpaSuccessFile.getId(), batchDealData),
                            actionPool);
                    batchData.clear();
                    futures.add(future);
                }
                totalCount++;
            }
            if (!batchData.isEmpty()) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                                dbDealBatchLine(tcyrCpaSuccessFile.getApiCode(), tcyrCpaSuccessRecord.getBatchNo(),
                                        tcyrCpaSuccessRecord.getData(), tcyrCpaSuccessFile.getId(), batchData),
                        actionPool);
                futures.add(future);
            }
            //3.修改csvFile totalCount数量、dbDeal状态、
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            tcyrCpaSuccessFileMapper.updateColliDingDataDealStatusAndTotalCount(tcyrCpaSuccessFile.getId(),
                    TcCpaCollidingDealStatusEnum.DEAL_SUCCESS.getValue(), totalCount);
            TcyrCpaCollectTask tcyrCpaCollectTask = TcyrCpaCollectTask.builder().batchNo(tcyrCpaSuccessFile.getBatchNo())
                    .status(TcCpaSyncDealStatusEnum.DEAL_NO.getValue()).sourceId(tcyrCpaSuccessFile.getId()).isDel(1)
                    .extend(tcyrCpaSuccessFile.getExtend()).sourceType(TcCpaCollidingSourceTypeEnum.SUCCESS.getValue())
                    .createTime(new Date()).updateTime(new Date())
                    .apiCode(marketingCommonConfig.getTcyrCpaApiCode()).build();
            tcyrCpaCollectTaskMapper.insert(tcyrCpaCollectTask);
        } catch (IOException e) {
            //4.修改quick_deal_status 异常状态
            tcyrCpaSuccessFileMapper.updateColliDingDataDealStatus(tcyrCpaSuccessFile.getId(),TcCpaCollidingDealStatusEnum.DEAL_FAIL.getValue());
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
        log.warn("TITLE:{},sync_file_id:{} db_deal执行结束,耗时:{}", TITLE, tcyrCpaSuccessFile.getId(), System.currentTimeMillis() - startTime);
    }

    private void dbDealBatchLine(String apiCode, String batchNo, String customerData, Long syncFileId, List<String> batchData) {
        List<List<String>> partitionList = ListUtils.partition(
                batchData, marketingCommonConfig.getTcyrCpaCollidingDealShardConfig().getInteger("dbPartSize"));

        JSONObject customJson = JSONObject.parseObject(customerData);
        SimpleDateFormat sdf = new SimpleDateFormat(DateHelper.LINE_DATE_FORMAT);
        for (List<String> itemList : partitionList) {
            try {
                Date nowDate = new Date();
                List<MarketingTcyrCpaSuccessData> tcyrCpaSuccessDataList = new ArrayList<>();
                for(String line : itemList) {
                    MarketingTcyrCpaSuccessData successDataItem = new MarketingTcyrCpaSuccessData();
                    successDataItem.setApiCode(apiCode);
                    successDataItem.setBatchNo(batchNo);
                    successDataItem.setSyncFileId(syncFileId);
                    successDataItem.setOriginText(line);
                    successDataItem.setCreateTime(nowDate);
                    successDataItem.setUpdateTime(nowDate);
                    successDataItem.setStartDate(sdf.parse(customJson.getString("startDate")));
                    successDataItem.setEndDate(sdf.parse(customJson.getString("endDate")));
                    JSONObject extentJson = new JSONObject();
                    String[] lineData = line.split(",");
                    if (lineData.length == 0) {
                        successDataItem.setUserKey(line);
                        successDataItem.setStatus(0);
                        successDataItem.setStatusMsg("原始数据异常");
                    }else {
                        String userKey = lineData[0].trim();
                        successDataItem.setUserKey(userKey);
                        successDataItem.setStatus(1);
                        String cell = custCellMappingService.selectCell(userKey);
                        if (StringUtils.isNotBlank(cell)) {
                            successDataItem.setCell(cell);
                            successDataItem.setIsMatch(TcCpaMatchStatusEnum.MATCH_SUCCESS.getValue());
                        }else{
                            successDataItem.setIsMatch(TcCpaMatchStatusEnum.MATCH_NO.getValue());
                        }
                        List<String> tcyrSyncExcludeFieldList = marketingCommonConfig.getTcyrCpaSyncSaveExcludeFieldList();
                        for (String key : customJson.keySet()) {
                            if (!tcyrSyncExcludeFieldList.contains(key)) {
                                extentJson.put(key, customJson.get(key));
                            }
                        }
                    }
                    extentJson.put("syncFileId", syncFileId);
                    successDataItem.setExtend(extentJson.toJSONString());
                    successDataItem.setIsDel(TcCpaIsDelEnum.DEL_NO.getValue());
                    tcyrCpaSuccessDataList.add(successDataItem);
                }
                tcyrCpaSuccessDataMapper.batchSave(tcyrCpaSuccessDataList);
                tcyrCpaSuccessDataList.clear();
            }catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "同程cpa撞库成功数据插入异常，fileId:" + syncFileId + "，lines:" + itemList, TITLE));
            }
        }
    }

}
