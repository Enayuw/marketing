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
import com.br.marketing.mapper.CallRecordMapper;
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
import java.time.LocalDate;
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

    @Resource
    private CallRecordMapper callRecordMapper;

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
            
            // 2.批量查询task_name（打标）
            Map<String, String> userKeyToTaskNameMap = batchQueryTaskName(apiCode, userKeyList);
            
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
                        // 打标：设置datapacket字段（task_name）
                        String taskName = userKeyToTaskNameMap.get(userKey);
                        if (StringUtils.isNotBlank(taskName)) {
                            extentJson.put("datapacket", taskName);
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

    /**
     * 批量查询task_name（打标）
     * 优化：使用真正的批量查询，避免N+1问题
     * @param apiCode apiCode
     * @param userKeyList 用户唯一编号列表
     * @return userKey -> taskName 的映射
     */
    private Map<String, String> batchQueryTaskName(String apiCode, List<String> userKeyList) {
        Map<String, String> userKeyToTaskNameMap = new HashMap<>();
        if (userKeyList == null || userKeyList.isEmpty()) {
            return userKeyToTaskNameMap;
        }
        
        try {
            // 1. 获取周期配置
            String[] dateRange = getCyclePeriodDateRange();
            String startDate = dateRange[0];
            String endDate = dateRange[1];
            
            // 2. 过滤空值
            List<String> validUserKeyList = userKeyList.stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            
            if (validUserKeyList.isEmpty()) {
                return userKeyToTaskNameMap;
            }
            
            // 3. 真正的批量查询（一次SQL查询所有数据）
            List<Map<String, String>> taskNameList = callRecordMapper.queryTaskNameByUserKeyList(
                    apiCode, validUserKeyList, startDate, endDate);
            
            // 4. 转换为Map
            for (Map<String, String> item : taskNameList) {
                String caseNum = item.get("caseNum");
                String taskName = item.get("taskName");
                if (StringUtils.isNotBlank(caseNum) && StringUtils.isNotBlank(taskName)) {
                    userKeyToTaskNameMap.put(caseNum, taskName);
                }
            }
                    
        } catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    "批量查询task_name异常，apiCode:" + apiCode + "," + e.getMessage(), TITLE), e);
        }
        
        return userKeyToTaskNameMap;
    }

    /**
     * 获取周期日期范围
     * 优先读取speed配置tcyrCpaCyclePeriodConfig，若为空则取上一自然周（周一至周日）
     * @return [startDate, endDate] 格式：["2026-01-19", "2026-01-26 23:59:59"]
     */
    private String[] getCyclePeriodDateRange() {
        // 1. 尝试从配置读取
        String cycleConfig = marketingCommonConfig.getTcyrCpaCyclePeriodConfig();
        if (StringUtils.isNotBlank(cycleConfig)) {
            // 解析格式：startDate,endDate，例如："2026-01-19,2026-01-25"
            String[] dates = cycleConfig.split(",");
            if (dates.length == 2) {
                String startDate = dates[0].trim();
                String endDate = dates[1].trim();
                if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
                    // 确保endDate包含时间部分
                    if (!endDate.contains(" ")) {
                        endDate = endDate + " 23:59:59";
                    }
                    return new String[]{startDate, endDate};
                }
            } else {
                log.warn("{} 配置格式错误，应为：startDate,endDate，例如：2026-01-19,2026-01-25，当前配置：{}", TITLE, cycleConfig);
            }
        }
        
        // 2. 配置为空或格式错误，计算上一自然周（周一至周日）
        return getLastNaturalWeekRange();
    }

    /**
     * 计算上一自然周（周一至周日）的日期范围
     * @return [startDate, endDate] 格式：["2026-01-19", "2026-01-26 23:59:59"]
     */
    private String[] getLastNaturalWeekRange() {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        
        // 获取本周一
        LocalDate thisMonday = today.with(java.time.DayOfWeek.MONDAY);
        
        // 上一自然周的周一（本周一减去7天）
        LocalDate lastMonday = thisMonday.minusWeeks(1);
        
        // 上一自然周的周日（上一周一加6天）
        LocalDate lastSunday = lastMonday.plusDays(6);
        
        String startDate = lastMonday.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = lastSunday.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:59";
        return new String[]{startDate, endDate};
    }

}
