package com.br.marketing.service.tccpa.impl;

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
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaMatchStatusEnum;
import com.br.marketing.enums.TcCpaSyncDealStatusEnum;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessFileMapper;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessRecordMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.service.tccpa.TcCpaCustCellMappingService;
import com.br.marketing.service.tccpa.TcCpaSyncDataQuickDealService;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TcCpaSyncDataQuickDealServiceImpl implements TcCpaSyncDataQuickDealService {

    private final static String TITLE = "【同程易融CPA-syncQuickDealShard任务】";

    private static final Random random = new Random();

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    private GeneralDataCleanService generalDataCleanService;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    private TcCpaCustCellMappingService custCellMappingService;

    @Resource
    private MarketingTcyrCpaSuccessRecordMapper tcyrCpaSuccessRecordMapper;

    @Resource
    private MarketingTcyrCpaSuccessFileMapper tcyrCpaSuccessFileMapper;

    @Override
    public void shardProcess(String apiCode) {
        String lockKey = RedisKeyConstant.tcyrCpaSyncQuickDeal.concat(apiCode);
        String lockValue =UUID.randomUUID().toString();
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_CPA_SYNC_DEAL.getName(), 2, 2);
        List<String> fileHeads = marketingCommonConfig.getTcyrCpaSuccessFileHeads();
        try {
            for (;;) {
                if (!marketingCommonConfig.getTcyrCpaSyncQuickDealShardConfig().getBoolean("jobSwitch")) {
                    break;
                }
                //1.抢锁
                redisChgService.lockLoop(lockKey, lockValue, 5000L, null);
                //2.查询单条未处理的csvFile
                MarketingTcyrCpaSuccessFile tcyrCpaSuccessFile = tcyrCpaSuccessFileMapper.selectSyncNoDealSingleFile(apiCode, TcCpaSyncDealStatusEnum.DEAL_NO.getValue());
                if (ObjectUtil.isEmpty(tcyrCpaSuccessFile)) {
                    redisChgService.unlock(lockKey, lockValue);
                    break;
                }
                //3.修改文件quickDeal处理状态-释放锁
                tcyrCpaSuccessFileMapper.updateSyncDataDealStatus(tcyrCpaSuccessFile.getId(),TcCpaSyncDealStatusEnum.DEAL_MIDDLE.getValue());
                redisChgService.unlock(lockKey, lockValue);
                //4.csvFile 快速处理流程
                csvFileQuickDeal(tcyrCpaSuccessFile, fileHeads, actionPool);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        } finally {
            //5、异常时释放锁(finally)
            redisChgService.unlock(lockKey, lockValue);
            actionPool.shutdownAndAwaitTermination();
        }
    }

    private void csvFileQuickDeal(MarketingTcyrCpaSuccessFile tcyrCpaFile, List<String> fileHeads, TpDynamicExecutor actionPool) {
        long startTime = System.currentTimeMillis();
        log.warn("TITLE:{},file_id:{} cpa_sync_deal执行", TITLE, tcyrCpaFile.getId());
        //1.判断文件存在
        File csvFile = new File(tcyrCpaFile.getFilePath());
        if (!csvFile.exists()) {
            tcyrCpaSuccessFileMapper.updateSyncDataDealStatus(tcyrCpaFile.getId(), TcCpaSyncDealStatusEnum.NO_FILE.getValue());
            return;
        }
        MarketingTcyrCpaSuccessRecord tcyrCpaSuccessRecord = tcyrCpaSuccessRecordMapper.selectByPrimaryKey(tcyrCpaFile.getSyncRecordId());
        //2.csvFileQuickDeal流程
        AtomicLong successCount = new AtomicLong(0L);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            List<String> batchData = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                batchData.add(line);
                if (batchData.size() == marketingCommonConfig.getTcyrCpaSyncQuickDealShardConfig().getInteger("pageSize")) {
                    if (!marketingCommonConfig.getTcyrCpaSyncQuickDealShardConfig().getBoolean("jobSwitch")) {
                        batchData.clear();
                        break;
                    }
                    Integer randomNumber = 10000 + random.nextInt(90000);
                    List<String> batchDealData = new ArrayList<>(batchData);
                    futures.add(CompletableFuture.runAsync(() ->
                            quickDealBatchLine(
                                    tcyrCpaFile.getApiCode(),
                                    tcyrCpaSuccessRecord.getBatchNo(),
                                    tcyrCpaSuccessRecord.getData(),
                                    tcyrCpaFile.getId(),
                                    batchDealData,
                                    successCount,
                                    randomNumber,
                                    fileHeads
                            ),actionPool));
                    batchData.clear();
                }
            }
            if (!batchData.isEmpty()) {
                Integer randomNumber = 10000 + random.nextInt(90000);
                List<String> batchDealData = new ArrayList<>(batchData);
                futures.add(CompletableFuture.runAsync(() ->
                        quickDealBatchLine(tcyrCpaFile.getApiCode(),
                                tcyrCpaSuccessRecord.getBatchNo(),
                                tcyrCpaSuccessRecord.getData(),
                                tcyrCpaFile.getId(),
                                batchDealData,
                                successCount,
                                randomNumber,
                                fileHeads
                        ),actionPool));
                batchData.clear();
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            //3.修改csvFile quickDeal状态、successCount
            tcyrCpaSuccessFileMapper.updateSyncDealStatusAndSuccesCount(tcyrCpaFile.getId(), TcCpaSyncDealStatusEnum.DEAL_SUCCESS.getValue(), successCount.get());
        } catch (IOException e) {
            //4.修改quick_deal_status 异常状态
            tcyrCpaSuccessFileMapper.updateSyncDataDealStatus(tcyrCpaFile.getId(), TcCpaSyncDealStatusEnum.DEAL_FAIL.getValue());
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
        log.warn("TITLE:{},file_id:{} cpa_sync_deal执行,time:{}", TITLE, tcyrCpaFile.getId(),System.currentTimeMillis()-startTime);
    }


    /**
     * 批次数据处理，匹配封装->上传清洗->上传调用
     */
    private void quickDealBatchLine(String apiCode, String batchNo, String customerData,
                                    Long syncFileId, List<String> batchData,
                                    AtomicLong successCount, Integer randomNumber, List<String> fileHeads) {
        try {
            // 1.数据匹配和封装
            List<MarketingTcyrCpaSuccessData> tcyrSyncList =
                    processBatchData(apiCode, batchNo, customerData, syncFileId, batchData, fileHeads);
            if (tcyrSyncList.isEmpty()) {
                return;
            }
            // 2.上传清洗
            List<JSONObject> jsonObjectList = JSON.parseArray(JSON.toJSONString(tcyrSyncList), JSONObject.class);
            Result callResult = generalDataCleanService.uploadClean(jsonObjectList, apiCode);
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
                    }
                }catch (Exception e) {
                    String pushResultStr = pushResult==null?e.getMessage():JSON.toJSONString(pushResult);
                    log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                            "上传推送异常,syncFileId:"+syncFileId+","+e.getMessage(), TITLE), e);
                }
            } else {
                log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        "数据清洗失败,syncFileId:"+syncFileId+","+callResult.getMessage(), TITLE));
            }
        }catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    "批次处理异常,syncFileId:"+syncFileId+","+e.getMessage(), TITLE), e);
        }
    }

    /**
     * 数据匹配与封装
     */
    private List<MarketingTcyrCpaSuccessData> processBatchData(
            String apiCode, String batchNo, String customerData, Long syncFileId, List<String> batchData, List<String> fileHeads) {
        JSONObject customJson = JSONObject.parseObject(customerData);
        SimpleDateFormat sdf = new SimpleDateFormat(DateHelper.LINE_DATE_FORMAT);
        List<MarketingTcyrCpaSuccessData> tcyrSyncList = new ArrayList<>();
        try {
            //1.id维度查 3、封装数据
            Map<String, String> userKeyToCellMap = new HashMap<>();
            List<String> userKeyList = batchData.stream()
                    .map(line -> line.split(","))
                    .filter(lineData -> lineData.length >= 1 && StringUtils.isNotBlank(lineData[0].trim()))
                    .map(lineData -> lineData[0].trim())
                    .collect(Collectors.toList());
            // id维度批量查库处理
            List<Map<String, Object>> cellList = custCellMappingService.selectCellInfo(userKeyList);
            for (Map<String, Object> map : cellList) {
                userKeyToCellMap.put(map.get("custNum").toString(), map.get("cell").toString());
            }
            // 3.遍历 batchData，命中才封装
            for (String line : batchData) {
                String[] lineData = line.split(",");
                if (lineData.length >= 1) {
                    String userKey = lineData[0].trim();
                    String cell = userKeyToCellMap.get(userKey);
                    if (StringUtils.isNotBlank(cell)) {
                        MarketingTcyrCpaSuccessData syncItem = new MarketingTcyrCpaSuccessData();
                        syncItem.setApiCode(apiCode);
                        syncItem.setBatchNo(batchNo);
                        syncItem.setSyncFileId(syncFileId);
                        syncItem.setUserKey(userKey);
                        syncItem.setIsMatch(TcCpaMatchStatusEnum.MATCH_SUCCESS.getValue());
                        syncItem.setCell(cell);
                        JSONObject extentJson = new JSONObject();
                        List<String> tcyrSyncExcludeFieldList = marketingCommonConfig.getTcyrCpaSyncSaveExcludeFieldList();
                        for (String key : customJson.keySet()) {
                            if (!tcyrSyncExcludeFieldList.contains(key)) {
                                extentJson.put(key, customJson.get(key));
                            }
                        }
                        syncItem.setStartDate(sdf.parse(customJson.getString("startDate")));
                        syncItem.setEndDate(sdf.parse(customJson.getString("endDate")));
                        extentJson.put("syncFileId", syncFileId);
                        //将所有列输出为扩展字段
                        for (int i = 0; i < Math.min(lineData.length, fileHeads.size()); i++) {
                            extentJson.put(fileHeads.get(i), lineData[i]);
                        }
                        syncItem.setExtend(extentJson.toJSONString());
                        tcyrSyncList.add(syncItem);
                    }
                }
            }
        }catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    "批次处理异常,syncFileId:"+syncFileId+","+e.getMessage(), TITLE), e);
        }
        return tcyrSyncList;
    }


    /**
     * 封装异步调用上传的数据
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

}
