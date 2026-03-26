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
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingTcyrErrorInterfaceLog;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncFile;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrErrorInterfaceLogMapper;
import com.br.marketing.mapper.MarketingTcyrSyncFileMapper;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.service.tc.TcSyncDataQuickDealService;
import com.br.marketing.service.tccpa.TcCpaCustCellMappingService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 同程易融快速处理流程(file->上传明细表)
 * @author zhiyong.zhang
 * @date 2025/07/03
 */
@Service
@Slf4j
public class TcSyncDataQuickDealServiceImpl implements TcSyncDataQuickDealService {

    private final static String TITLE = "【同程易融-quickDealShard任务】";

    private static final Random random = new Random();


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
    private MarketingTcyrErrorInterfaceLogMapper errorInterfaceLogMapper;

    @Resource
    private TcCpaCustCellMappingService tcCpaCustCellMappingService;


    @Override
    public void shardProcess(String apiCode) {
        String lockKey = RedisKeyConstant.tcyrQuickDeal.concat(apiCode);
        String lockValue = "";
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_QUICK_DEAL.getName(), 2,2);
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
                    break;
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
            actionPool.shutdownAndAwaitTermination();
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
            tcyrSyncFileMapper.updateQuickDealStatus(tcyrSyncFile.getId(), 4);
            return;
        }
        MarketingTcyrSyncRecord syncRecord = tcyrSyncRecordMapper.selectByPrimaryKey(tcyrSyncFile.getSyncRecordId());
        if (syncRecord == null) {
            tcyrSyncFileMapper.updateQuickDealStatus(tcyrSyncFile.getId(), 3);
            return;
        }
        String scene = StringUtils.isNotBlank(syncRecord.getScene())
                ? syncRecord.getScene()
                : resolveScene(syncRecord.getBatchNo());
        if ("NEW".equals(scene)) {
            tcyrSyncFileMapper.updateQuickDealAndSuccesCount(tcyrSyncFile.getId(), 2, 0L);
            return;
        }
        //2.csvFileQuickDeal流程
        AtomicLong successCount = new AtomicLong(0L);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            List<String> batchData = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                batchData.add(line);
                if (batchData.size() == marketingCommonConfig.getTcQuickDealShardConfig().getInteger("pageSize")) {
                    if (!marketingCommonConfig.getTcQuickDealShardConfig().getBoolean("jobSwitch")) {
                        batchData.clear();
                        break;
                    }
                    Integer randomNumber = 10000 + random.nextInt(90000);
                    List<String> batchDealData = new ArrayList<>(batchData);
                    futures.add(CompletableFuture.runAsync(() ->
                                    quickDealBatchLine(tcyrSyncFile.getApiCode(), syncRecord.getBatchNo(),
                                            syncRecord.getData(), scene, tcyrSyncFile.getId(),batchDealData, successCount,randomNumber
                                    ),actionPool));
                    batchData.clear();
                }
            }
            if (!batchData.isEmpty()) {
                Integer randomNumber = 10000 + random.nextInt(90000);
                List<String> batchDealData = new ArrayList<>(batchData);
                futures.add(CompletableFuture.runAsync(() ->
                                quickDealBatchLine(tcyrSyncFile.getApiCode(), syncRecord.getBatchNo(),
                                        syncRecord.getData(), scene, tcyrSyncFile.getId(),batchDealData, successCount,randomNumber
                                ),actionPool));
                batchData.clear();
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            //3.修改csvFile quickDeal状态、successCount
            tcyrSyncFileMapper.updateQuickDealAndSuccesCount(tcyrSyncFile.getId(),2,successCount.get());
        } catch (IOException e) {
            //4.修改quick_deal_status 异常状态
            tcyrSyncFileMapper.updateQuickDealStatus(tcyrSyncFile.getId(),3);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
        log.warn("TITLE:{},sync_file_id:{} quick_deal执行结束,耗时:{},成功处理数量:{}",
                TITLE, tcyrSyncFile.getId(), System.currentTimeMillis() - startTime, successCount.get());
    }

    /**
     * 批次数据处理，匹配封装->上传清洗->上传调用
     */
    private void quickDealBatchLine(String apiCode, String batchNo, String customerData, String scene,
                                    Long syncFileId, List<String> batchData,
                                    AtomicLong successCount,Integer randomNumber) {
        long startTime = System.currentTimeMillis();
        try {
            // 1.数据匹配和封装
            List<MarketingTcyrSync> tcyrSyncList = processBatchData(apiCode, batchNo, customerData, syncFileId, batchData);
            if (tcyrSyncList.isEmpty()) {
                return;
            }
            // 2.上传清洗
            List<JSONObject> jsonObjectList = JSON.parseArray(JSON.toJSONString(tcyrSyncList), JSONObject.class);
            Result callResult = StringUtils.isNotBlank(scene)
                    ? generalDataCleanService.uploadClean(jsonObjectList, apiCode, scene)
                    : generalDataCleanService.uploadClean(jsonObjectList, apiCode);
            if (callResult!=null && callResult.isSuccess()) {
                String requestId = apiCode+"_"+batchNo+"_"+System.currentTimeMillis()+"_"+randomNumber;
                //3.调用定制化上传接口
                List<MarketingPreUserDetailDTO> marketingPreUserDetailDTOS = (List<MarketingPreUserDetailDTO>) callResult.getData();
                UploadDataDTO uploadDataDTO = initUploadData(apiCode,batchNo, marketingPreUserDetailDTOS,requestId);
                Result<Boolean> pushResult = new Result<>();
                try {
                    pushResult = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                    if (pushResult != null && pushResult.isSuccess()) {
                        successCount.addAndGet(tcyrSyncList.size());
                    } else {
                        log.error("TITLE:{},上传请求失败，syncFileId: {}, 数据量: {}, resultMsg: {}",
                                TITLE,syncFileId, tcyrSyncList.size(), pushResult.getMessage());
                        saveErrorIneterfaceLog(apiCode,batchNo,syncFileId,marketingPreUserDetailDTOS.size(),
                                JSONObject.toJSONString(uploadDataDTO),JSONObject.toJSONString(pushResult),1,requestId);
                    }
                }catch (Exception e) {
                    String pushResultStr = pushResult==null?e.getMessage():JSON.toJSONString(pushResult);
                    log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                            "上传推送异常,syncFileId:"+syncFileId+","+e.getMessage(), TITLE), e);
                    saveErrorIneterfaceLog(apiCode,batchNo,syncFileId,marketingPreUserDetailDTOS.size(),
                            JSONObject.toJSONString(uploadDataDTO),pushResultStr,2,requestId);
                }
            } else {
                log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        "数据清洗失败,syncFileId:"+syncFileId+","+callResult.getMessage(), TITLE));
            }
        }catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    "批次处理异常,syncFileId:"+syncFileId+","+e.getMessage(), TITLE), e);
        }
        if (marketingCommonConfig.getTcQuickDealShardConfig().getBoolean("detailLogSwitch")) {
            log.warn("TITLE:{},sync_file_id:{} quick_deal 单批次执行结束,耗时:{},成功处理数量:{}",
                    TITLE, syncFileId, System.currentTimeMillis() - startTime, batchData.size());
        }
    }

    /**
     * 数据匹配与封装
     */
    private List<MarketingTcyrSync> processBatchData(String apiCode, String batchNo, String customerData, Long syncFileId, List<String> batchData) {
        List<MarketingTcyrSync> tcyrSyncList = new ArrayList<>();
        if(marketingCommonConfig.getTcQuickDealShardConfig().getBoolean("detailSingleSwitch")) {
            batchData.forEach(line -> {
                String[] lineData = line.split(",");
                if (lineData.length >=2) {
                    String userKey = lineData[0].trim();
                    if (StringUtils.isEmpty(userKey)) {
                        return;
                    }
                    String cell = tcCpaCustCellMappingService.selectCell(userKey);
                    if (StringUtils.isNotBlank(cell)) {
                        String terminal = lineData[1].trim();
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
        } else {
            //1.id维度查  2、封装数据
            Map<String, String> userKeyToCellMap = new HashMap<>();
            List<String> userKeyList = batchData.stream()
                    .map(line -> line.split(","))
                    .filter(lineData -> lineData.length >= 2 && StringUtils.isNotBlank(lineData[0].trim()))
                    .map(lineData -> lineData[0].trim())
                    .collect(Collectors.toList());
            // 1.id维度批量查库处理
            List<Map<String, Object>> cellList  = tcCpaCustCellMappingService.selectCellInfo(userKeyList);
            for (Map<String, Object> map : cellList) {
                userKeyToCellMap.put(map.get("custNum").toString(), map.get("cell").toString());
            }
            // 2.遍历 batchData，命中才封装
            for (String line : batchData) {
                String[] lineData = line.split(",");
                if (lineData.length >= 2) {
                    String userKey = lineData[0].trim();
                    String terminal = lineData[1].trim();
                    String cell = userKeyToCellMap.get(userKey);
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
            }
        }
        return tcyrSyncList;
    }

    /**
     * 封装异步调用上传的数据
     *
     * @param apiCode   apiCode
     * @param syncUsers 具体数据对象
     */
    private UploadDataDTO initUploadData(String apiCode,String batchNo, List<MarketingPreUserDetailDTO> syncUsers,String requestId) {
        String taskId = batchNo;
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
    private void saveErrorIneterfaceLog(String apiCode, String batchNo, Long syncFileId, Integer elementSize, String requestParam, String pushResult,
                                        Integer errorType,String requestId) {
        MarketingTcyrErrorInterfaceLog errorInterfaceLog = new MarketingTcyrErrorInterfaceLog();
        errorInterfaceLog.setApiCode(apiCode);
        errorInterfaceLog.setBatchNo(batchNo);
        errorInterfaceLog.setSyncFileId(syncFileId);
        errorInterfaceLog.setElementCount(elementSize);
        errorInterfaceLog.setRequestParam(requestParam);
        errorInterfaceLog.setPushResult(pushResult);
        errorInterfaceLog.setErrorType(errorType);
        errorInterfaceLog.setRequestId(requestId);
        errorInterfaceLog.setCreateTime(new Date());
        errorInterfaceLog.setUpdateTime(new Date());
        try {
            errorInterfaceLogMapper.insertSelective(errorInterfaceLog);
        } catch (Exception e) {
            log.error("saveErrorIneterfaceLog{}",JSONObject.toJSONString(errorInterfaceLog), e);
        }
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

    private String resolveScene(String batchNo) {
        Map<String, String> sceneMap = marketingCommonConfig.getTcBatchNoSuffixToSceneConfig();
        if (StringUtils.isBlank(batchNo) || sceneMap == null || sceneMap.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : sceneMap.entrySet()) {
            String prefix = entry.getKey();
            if (StringUtils.isBlank(prefix)) {
                continue;
            }
            if (batchNo.startsWith(prefix)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
