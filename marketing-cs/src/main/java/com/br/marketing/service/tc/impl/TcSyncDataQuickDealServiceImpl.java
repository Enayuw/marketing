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
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncFile;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrCustCellMappingMapper;
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

//TODO 需排查所有历史代码影响
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
    MarketingTcyrCustCellMappingMapper tcyrCustCellMappingMapper;


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
                    log.error("{}获取锁最终失败，apiCode:{}，已重试{}次，错误信息:{}", TITLE, lockKey, maxRetryTimes, e.getMessage());
                }
            }
        }
        return false;
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
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            List<String> batchData = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                batchData.add(line);
                if (batchData.size() == marketingCommonConfig.getTcQuickDealShardConfig().getInteger("batchSize")) {
                    modifyThreadPool(actionPool);
                    List<String> batchDealData = new ArrayList<>(batchData);
                    actionPool.submit(()->quickDealBatchLine(tcyrSyncFile.getApiCode(),syncRecord.getBatchNo(),syncRecord.getData(),tcyrSyncFile.getId(),batchDealData));
                    batchData.clear();
                }
            }
            if (!batchData.isEmpty()) {
                actionPool.submit(()->quickDealBatchLine(tcyrSyncFile.getApiCode(),syncRecord.getBatchNo(),syncRecord.getData(),tcyrSyncFile.getId(),batchData));
            }
            //3.修改csvFile quickDeal流程完成状态
            tcyrSyncFileMapper.updateQuickDealStatus(tcyrSyncFile.getId(),2);
        } catch (IOException e) {
            //4.修改quick_deal_status 异常状态
            tcyrSyncFileMapper.updateQuickDealStatus(tcyrSyncFile.getId(),3);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
        log.warn("TITLE:{},sync_file_id:{} quick_deal执行结束,耗时:{}", TITLE, tcyrSyncFile.getId(), System.currentTimeMillis() - startTime);
    }

    /**
     * 批次数据处理，匹配封装->上传清洗->上传调用
     */
    private void  quickDealBatchLine(String apiCode,String batchNo,String customerData,Long syncFileId,List<String> batchData){
        try {
            // 1.数据匹配和封装
            List<MarketingTcyrSync> tcyrSyncList = processBatchData(apiCode, batchNo, customerData, syncFileId, batchData);
            if (tcyrSyncList.isEmpty()) {
                return;
            }
            // 2.上传清洗
            List<List<MarketingTcyrSync>> partitionList = ListUtils.partition(tcyrSyncList, 1000);
            for(List<MarketingTcyrSync> tcyrSyncItemList : partitionList){
                List<JSONObject> jsonObjectList = JSON.parseArray(JSON.toJSONString(tcyrSyncItemList), JSONObject.class);
                Result callResult = generalDataCleanService.uploadClean(jsonObjectList, apiCode);
                if (callResult!=null && callResult.isSuccess()) {
                    //3.调用定制化上传接口
                    List<MarketingPreUserDetailDTO> marketingPreUserDetailDTOS = (List<MarketingPreUserDetailDTO>) callResult.getData();
                    UploadDataDTO uploadDataDTO = initUploadData(apiCode,batchNo, marketingPreUserDetailDTOS);
                    Result<Boolean> pushResult = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                    log.warn("{},apiCode:{},batchNo:{},syncDataClen调用push接口结果:{}", TITLE,apiCode,batchNo,JSONObject.toJSONString(pushResult));
                    if (pushResult == null || !pushResult.isSuccess()) {
                        //TODO 记录上传清洗的异常请求
                        log.error("{}推送失败，apiCode:{}, batchNo:{}, 错误信息:{}", TITLE, apiCode, batchNo, pushResult != null ? pushResult.getMessage() : "pushResult为null");
                    }
                }
            }
        }catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    /**
     * 动态调整线程池大小
     */
    private void modifyThreadPool(ThreadPoolExecutor actionPool) {
        Integer threadNum = marketingCommonConfig.getTcQuickDealShardConfig().getInteger("threadPool");
        Integer corePoolSize = actionPool.getCorePoolSize();
        // 只在配置真正发生变化时才调整线程池
        if (!corePoolSize.equals(threadNum)) {
            log.info(TITLE + "检测到线程池配置变更，调整线程池大小: {} -> {}", corePoolSize, threadNum);
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
                String cell = tcyrCustCellMappingMapper.selectCustNumtiflash_(userKey);
                if (StringUtils.isNotBlank(cell)) {
                    MarketingTcyrSync syncItem = new MarketingTcyrSync();
                    syncItem.setBatchNo(batchNo);
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
                    syncItem.setApiCode(apiCode);
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
