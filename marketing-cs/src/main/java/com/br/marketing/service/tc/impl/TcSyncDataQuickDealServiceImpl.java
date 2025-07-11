package com.br.marketing.service.tc.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingTcyrErrorInterfaceLog;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncFile;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrCustCellMappingMapper;
import com.br.marketing.mapper.MarketingTcyrErrorInterfaceLogMapper;
import com.br.marketing.mapper.MarketingTcyrSyncFileMapper;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.service.tc.TcSyncDataQuickDealService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 同程易融快速处理流程(file->上传明细表)
 * @author zhiyong.zhang
 * @date 2025/07/03
 */
@Service
@Slf4j
public class TcSyncDataQuickDealServiceImpl implements TcSyncDataQuickDealService {

    private final static String TITLE = "【同程易融-quickDealShard任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private GeneralDataCleanService generalDataCleanService;

    @Resource
    private PushInfoService pushInfoService;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    private MarketingTcyrSyncFileMapper tcyrSyncFileMapper;

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;

    @Resource
    private MarketingTcyrCustCellMappingMapper tcyrCustCellMappingMapper;

    @Resource
    private MarketingTcyrErrorInterfaceLogMapper errorInterfaceLogMapper;


    @Override
    public void shardProcess(String apiCode) {
        String lockKey = RedisKeyConstant.tcyrQuickDeal.concat(apiCode);;
        String lockValue = "";
        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(
                marketingCommonConfig.getTcQuickDealShardConfig().getInteger("threadPool"),
                marketingCommonConfig.getTcQuickDealShardConfig().getInteger("threadPool"));
        try {
            for (;;) {
                if (!marketingCommonConfig.getTcQuickDealShardConfig().getBoolean("jobSwitch")) {
                    break;
                }
                lockValue = UUID.randomUUID().toString();
                //1.抢锁 - 添加重试机制
                boolean lockAcquired = acquireLockWithRetry(lockKey, lockValue);
                if (!lockAcquired) {
                    log.warn("{}获取锁失败，apiCode:{}，跳过本次处理", TITLE, apiCode);
                    continue;
                }
                //2.查询单条未处理的csvFile(查询quick_deal_status=0,db_deal_status=0的数据)
                MarketingTcyrSyncFile tcyrSyncFile = tcyrSyncFileMapper.selectNoDealSingleSyncFile(apiCode, 0,0);
                if (ObjectUtil.isEmpty(tcyrSyncFile)) {
                    redisChgService.unlock(lockKey, lockValue);
                    break;
                }
                //3.修改文件quickDeal处理状态-释放锁
                tcyrSyncFileMapper.updateQuickDealStatus(tcyrSyncFile.getId(),1);
                redisChgService.unlock(lockKey, lockValue);
                //4.csvFile 快速处理流程
                csvFileQuickDeal(tcyrSyncFile,actionPool);
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }finally {
            //5、异常时释放锁(finally)
            redisChgService.unlock(lockKey, lockValue);
            shutdownThreadPool(actionPool);
        }
    }

    /**
     * 具体csvFile文件处理-读取文件
     * @param tcyrSyncFile
     * @param actionPool
     */
    private void csvFileQuickDeal(MarketingTcyrSyncFile tcyrSyncFile, ThreadPoolExecutor actionPool) {
        long startTime = System.currentTimeMillis();
        log.warn("TITLE:{},sync_file_id:{} quick_deal执行", TITLE, tcyrSyncFile.getId());
        //1.判断文件存在
        File txtFile = new File(tcyrSyncFile.getFilePath());
        if (!txtFile.exists()) {
            tcyrSyncFileMapper.updateQuickDealStatus(tcyrSyncFile.getId(), 3);
            return;
        }
        MarketingTcyrSyncRecord syncRecord = tcyrSyncRecordMapper.selectByPrimaryKey(tcyrSyncFile.getSyncRecordId());
        //2.csvFileQuickDeal流程
        AtomicLong successCount = new AtomicLong(0L);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            List<String> batchData = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                batchData.add(line);
                if (batchData.size() == marketingCommonConfig.getTcQuickDealShardConfig().getInteger("pageSize")) {
                    modifyThreadPool(actionPool);
                    List<String> batchDealData = new ArrayList<>(batchData);
                    futures.add(CompletableFuture.runAsync(() ->
                                    quickDealBatchLine(tcyrSyncFile.getApiCode(), syncRecord.getBatchNo(),
                                                        syncRecord.getData(), tcyrSyncFile.getId(),batchDealData, successCount
                                    ),
                                actionPool));
                    batchData.clear();
                }
            }
            if (!batchData.isEmpty()) {
                futures.add(CompletableFuture.runAsync(() ->
                                quickDealBatchLine(tcyrSyncFile.getApiCode(), syncRecord.getBatchNo(),
                                                    syncRecord.getData(),tcyrSyncFile.getId(),batchData, successCount
                                ),
                            actionPool));
                batchData.clear();
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            //3.修改csvFile quickDeal状态、successCount
            tcyrSyncFile.setSuccessCount(successCount.get());
            tcyrSyncFile.setQuickDealStatus(2);
            tcyrSyncFile.setUpdateTime(new Date());
            tcyrSyncFileMapper.updateByPrimaryKey(tcyrSyncFile);
        } catch (IOException e) {
            //4.修改quick_deal_status 异常状态
            tcyrSyncFileMapper.updateQuickDealStatus(tcyrSyncFile.getId(),3);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
        log.warn("TITLE:{},sync_file_id:{} quick_deal执行结束,耗时:{},成功处理数量:{}", TITLE, tcyrSyncFile.getId(), System.currentTimeMillis() - startTime, successCount.get());
    }

    /**
     * 批次数据处理，匹配封装->上传清洗->上传调用
     */
    private void quickDealBatchLine(String apiCode, String batchNo, String customerData, Long syncFileId, List<String> batchData, AtomicLong successCount) {
        long startTime = System.currentTimeMillis();
        try {
            // 1.数据匹配和封装
            List<MarketingTcyrSync> tcyrSyncList = processBatchData(apiCode, batchNo, customerData, syncFileId, batchData);
            // 2.上传清洗
            List<List<MarketingTcyrSync>> partitionList = ListUtils.partition(tcyrSyncList, 1000);
            for(List<MarketingTcyrSync> tcyrSyncItemList : partitionList){
                List<JSONObject> jsonObjectList = JSON.parseArray(JSON.toJSONString(tcyrSyncItemList), JSONObject.class);
                Result callResult = generalDataCleanService.uploadClean(jsonObjectList, apiCode);
                if (callResult!=null && callResult.isSuccess()) {
                    //3.调用定制化上传接口
                    List<MarketingPreUserDetailDTO> marketingPreUserDetailDTOS = (List<MarketingPreUserDetailDTO>) callResult.getData();
                    UploadDataDTO uploadDataDTO = initUploadData(apiCode,batchNo, marketingPreUserDetailDTOS);
                    Result<Boolean> pushResult = new Result<>();
                    try {
                        pushResult = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                        if (pushResult != null && pushResult.isSuccess()) {
                            successCount.addAndGet(tcyrSyncItemList.size());
                        } else {
                            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                                    "上传推送失败,"+JSONObject.toJSONString(pushResult), TITLE));
                            saveErrorIneterfaceLog(apiCode,batchNo,syncFileId,marketingPreUserDetailDTOS.size(),
                                    JSONObject.toJSONString(uploadDataDTO),JSONObject.toJSONString(pushResult),1);
                        }
                    }catch (Exception e) {
                        log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                                "上传推送异常,"+e.getMessage(), TITLE), e);
                        saveErrorIneterfaceLog(apiCode,batchNo,syncFileId,marketingPreUserDetailDTOS.size(),
                                JSONObject.toJSONString(uploadDataDTO),JSONObject.toJSONString(pushResult),2);
                    }
                }
            }
        }catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
        if (marketingCommonConfig.getTcQuickDealShardConfig().getBoolean("detailLogSwitch")) {
            log.warn("TITLE:{},sync_file_id:{} quick_deal 单批次执行结束,耗时:{},成功处理数量:{}", TITLE, syncFileId, System.currentTimeMillis() - startTime, batchData.size());
        }
    }



    /**
     * 动态调整线程池大小
     */
    private void modifyThreadPool(ThreadPoolExecutor actionPool) {
        Integer threadNum = marketingCommonConfig.getTcQuickDealShardConfig().getInteger("threadPool");
        Integer corePoolSize = actionPool.getCorePoolSize();
        if (!corePoolSize.equals(threadNum)) {
            actionPool.setCorePoolSize(threadNum);
            actionPool.setMaximumPoolSize(threadNum);
        }
    }

    /**
     * 数据匹配与封装
     */
    private List<MarketingTcyrSync> processBatchData(String apiCode, String batchNo, String customerData, Long syncFileId, List<String> batchData) {
        List<MarketingTcyrSync> tcyrSyncList = new ArrayList<>();
        batchData.forEach(line -> {
            String[] lineData = line.split(",");
            if (lineData.length >=2) {
                String userKey = lineData[0].trim();
                String terminal = lineData[1].trim();
                String cell = tcyrCustCellMappingMapper.selectCelltikv_(userKey);
                if (StringUtils.isNotBlank(cell)) {
                    MarketingTcyrSync syncItem = new MarketingTcyrSync();
                    syncItem.setApiCode(apiCode);
                    syncItem.setBatchNo(batchNo);
                    syncItem.setSyncFileId(syncFileId);
                    syncItem.setUserKey(userKey);
                    syncItem.setTerminal(terminal);
                    syncItem.setIsMatch(1);
                    syncItem.setIsClean(0);
                    syncItem.setCell(cell);
                    JSONObject extentJson = new JSONObject();
                    JSONObject customJson = JSONObject.parseObject(customerData);
                    List<String> tcyrSyncExcludeFieldList = marketingCommonConfig.getTcyrSyncSaveExcludeFieldList();
                    for (String key : customJson.keySet()) {
                        if (!tcyrSyncExcludeFieldList.contains(key)) {
                            extentJson.put(key, customJson.get(key));
                        }
                    }
                    extentJson.put("syncFileId", syncFileId);
                    syncItem.setExtend(extentJson.toJSONString());
                    tcyrSyncList.add(syncItem);
                }
            }
        });
        return tcyrSyncList;
    }

    /**
     * 封装异步调用上传的数据
     *
     * @param apiCode   apiCode
     * @param syncUsers 具体数据对象
     */
    private UploadDataDTO initUploadData(String apiCode,String batchNo, List<MarketingPreUserDetailDTO> syncUsers) {
        String taskId = batchNo;
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000);
        String requestId = apiCode+"_"+taskId+"_"+System.currentTimeMillis()+"_"+randomNumber;
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(taskId);
        marketingPreUserDTO.setRequestId(requestId);
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }

    /**
     * 保存错误请求记录
     */
    private void saveErrorIneterfaceLog(String apiCode, String batchNo, Long syncFileId, Integer elementSize, String requestParam, String pushResult,Integer errorType) {
        MarketingTcyrErrorInterfaceLog errorInterfaceLog = new MarketingTcyrErrorInterfaceLog();
        errorInterfaceLog.setApiCode(apiCode);
        errorInterfaceLog.setBatchNo(batchNo);
        errorInterfaceLog.setSyncFileId(syncFileId);
        errorInterfaceLog.setElementCount(elementSize);
        errorInterfaceLog.setRequestParam(requestParam);
        errorInterfaceLog.setPushResult(pushResult);
        errorInterfaceLog.setErrorType(errorType);
        errorInterfaceLogMapper.insertSelective(errorInterfaceLog);
    }

    /**
     * 带重试机制的获取锁
     * @param lockKey 锁的key
     * @param lockValue 锁的值
     * @return 是否成功获取锁
     */
    private boolean acquireLockWithRetry(String lockKey, String lockValue) {
        int maxRetryTimes = marketingCommonConfig.getTcQuickDealShardConfig().getInteger("lockRetryTimes");
        long retryIntervalMs = marketingCommonConfig.getTcQuickDealShardConfig().getLong("lockRetryIntervalMs");
        for (int retryCount = 0; retryCount <= maxRetryTimes; retryCount++) {
            try {
                redisChgService.lock(lockKey, lockValue);
                return true;
            } catch (Exception e) {
                if (retryCount < maxRetryTimes) {
                    log.warn("{}获取锁失败，apiCode:{}，重试次数:{}/{}，错误信息:{}",
                            TITLE, lockKey, retryCount + 1, maxRetryTimes, e.getMessage());
                    try {
                        Thread.sleep(retryIntervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("{}重试等待被中断", TITLE);
                        return false;
                    }
                } else {
                    log.warn("{}获取锁最终失败，apiCode:{}，已重试{}次，错误信息:{}", TITLE, lockKey, maxRetryTimes, e.getMessage());
                }
            }
        }
        return false;
    }

    public  void shutdownThreadPool(ThreadPoolExecutor executor) {
        log.warn(TITLE + "shutdownThreadPool开始");
        executor.shutdown();
        try {
            while (!executor.awaitTermination(60L, TimeUnit.SECONDS)) {
                log.info("{},线程池关闭",TITLE);
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            log.error("{},日志保存线程池结束异常！",TITLE,ex);
            Thread.currentThread().interrupt();
        }
        log.warn(TITLE + "shutdownThreadPool结束");
    }
}
