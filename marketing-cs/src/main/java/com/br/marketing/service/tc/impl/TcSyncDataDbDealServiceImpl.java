package com.br.marketing.service.tc.impl;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.service.tccpa.TcCpaCustCellMappingService;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import org.apache.commons.collections4.ListUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTcyrSyncFile;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrSyncFileMapper;
import com.br.marketing.mapper.MarketingTcyrSyncMapper;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.tc.TcSyncDataDbDealService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
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

/**
 * 同程易融快速处理流程(file->原始数据表)
 * quickDeal流程完毕 此流程
 * @author zhiyong.zhang
 * @date 2025/07/05
 */
@Service
@Slf4j
public class TcSyncDataDbDealServiceImpl implements TcSyncDataDbDealService {

    private final static String TITLE = "【同程易融-dbDealShard任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrSyncFileMapper tcyrSyncFileMapper;

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;

    @Resource
    private MarketingTcyrSyncMapper tcyrSyncMapper;

    @Autowired
    private RedisChgService redisChgService;

    @Resource
    private TcCpaCustCellMappingService tcCpaCustCellMappingService;

    @Override
    public void shardProcess(String apiCode) {
        String lockKey = RedisKeyConstant.tcyrDbDeal.concat(apiCode);;
        String lockValue = "";
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_DB_DEAL.getName(), 50, 50);
        try {
            for (;;) {
                if (!marketingCommonConfig.getTcDbDealShardConfig().getBoolean("jobSwitch")) {
                    break;
                }
                lockValue = UUID.randomUUID().toString();
                //1.抢锁 - 添加重试机制
                boolean lockAcquired = acquireLockWithRetry(lockKey, lockValue);
                if (!lockAcquired) {
                    log.warn("{}获取锁失败，apiCode:{}，跳过本次处理", TITLE, apiCode);
                    break;
                }
                //2.查询单条未处理的csvFile(查询quick_deal_status=1,db_deal_status=0的数据)
                MarketingTcyrSyncFile tcyrSyncFile = tcyrSyncFileMapper.selectNoDealSingleSyncFile(apiCode, 2,0);
                if (ObjectUtil.isEmpty(tcyrSyncFile)) {
                    redisChgService.unlock(lockKey, lockValue);
                    break;
                }
                //3.修改文件quickDeal处理状态-释放锁
                tcyrSyncFileMapper.updateDbDealStatus(tcyrSyncFile.getId(),1);
                redisChgService.unlock(lockKey, lockValue);
                //4.csvFile 快速处理流程
                csvFileDbDeal(tcyrSyncFile,actionPool);
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

    private void csvFileDbDeal(MarketingTcyrSyncFile tcyrSyncFile, ThreadPoolExecutor actionPool) {
        long startTime = System.currentTimeMillis();
        log.warn("TITLE:{},sync_file_id:{} db_deal执行", TITLE, tcyrSyncFile.getId());
        //1.判断文件存在
        File txtFile = new File(tcyrSyncFile.getFilePath());
        if (!txtFile.exists()) {
            tcyrSyncFileMapper.updateDbDealStatus(tcyrSyncFile.getId(), 4);
            return;
        }
        MarketingTcyrSyncRecord syncRecord = tcyrSyncRecordMapper.selectByPrimaryKey(tcyrSyncFile.getSyncRecordId());
        //2.csvFileDbDeal流程
        long totalCount = 0L;
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            List<String> batchData = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                batchData.add(line);
                if (batchData.size() == marketingCommonConfig.getTcDbDealShardConfig().getInteger("pageSize")) {
                    if (!marketingCommonConfig.getTcDbDealShardConfig().getBoolean("jobSwitch")) {
                        batchData.clear();
                        break;
                    }
                    List<String> batchDealData = new ArrayList<>(batchData);
                    actionPool.submit(()->
                            dbDealBatchLine(tcyrSyncFile.getApiCode(),syncRecord.getBatchNo(),syncRecord.getData(),tcyrSyncFile.getId(),batchDealData)
                    );
                    batchData.clear();
                }
                totalCount++;
            }
            if (!batchData.isEmpty()) {
                actionPool.submit(()->
                        dbDealBatchLine(tcyrSyncFile.getApiCode(),syncRecord.getBatchNo(),syncRecord.getData(),tcyrSyncFile.getId(),batchData)
                );
            }
            //3.修改csvFile totalCount数量、dbDeal状态、
            tcyrSyncFile.setTotalCount(totalCount);
            tcyrSyncFile.setDbDealStatus(2);
            tcyrSyncFile.setUpdateTime(new Date());
            tcyrSyncFileMapper.updateByPrimaryKey(tcyrSyncFile);
        } catch (IOException e) {
            //4.修改quick_deal_status 异常状态
            tcyrSyncFileMapper.updateDbDealStatus(tcyrSyncFile.getId(),3);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
        log.warn("TITLE:{},sync_file_id:{} db_deal执行结束,耗时:{}", TITLE, tcyrSyncFile.getId(), System.currentTimeMillis() - startTime);
    }

    private void dbDealBatchLine(String apiCode, String batchNo, String customerData, Long syncFileId, List<String> batchData) {
        try {
            List<List<String>> partitionList = ListUtils.partition(
                    batchData, marketingCommonConfig.getTcDbDealShardConfig().getInteger("dbPartSize"));
            for (List<String> partitionItemList : partitionList) {
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("INSERT INTO b_marketing_tcyr_sync (api_code,batch_no,sync_file_id,user_key,terminal," +
                        "cell,is_match,extend,status,create_time,update_time) VALUES");
                int count = 0;
                for (String line : partitionItemList) {
                    if (count > 0) {
                        sqlBuilder.append(",");
                    }
                    String[] data = line.split(",");
                    sqlBuilder.append("('").append(escapeSqlString(apiCode)).
                            append("','").append(escapeSqlString(batchNo)).append("',").append(syncFileId);
                    if (data.length == 1) {
                        String userKey = data[0].trim();
                        sqlBuilder.append(",'").append(escapeSqlString(userKey)).append("',NULL,NULL,NULL");
                        JSONObject extentJson = new JSONObject();
                        for (int i = 0; i < data.length; i++) {
                            extentJson.put("column_" + (i + 1), data[i]);
                        }
                        sqlBuilder.append(",'").append(escapeSqlString(JSONObject.toJSONString(extentJson))).append("',0,NOW(),NOW())");
                    } else if (data.length >= 2) {
                        String userKey = data[0].trim();
                        sqlBuilder.append(",'").append(escapeSqlString(userKey)).append("','").append(escapeSqlString(data[1].trim())).append("'");
                        String cell = tcCpaCustCellMappingService.selectCell(userKey);
                        if (StringUtils.isNotBlank(cell)) {
                            sqlBuilder.append(",'").append(escapeSqlString(cell)).append("',1");
                        } else {
                            sqlBuilder.append(",NULL,0");
                        }
                        //extend
                        JSONObject extentJson = new JSONObject();
                        for (int i = 0; i < data.length; i++) {
                            extentJson.put("column_" + (i + 1), data[i]);
                        }
                        JSONObject customJson = JSONObject.parseObject(customerData);
                        List<String> tcyrSyncExcludeFieldList = marketingCommonConfig.getTcyrSyncSaveExcludeFieldList();
                        for (String key : customJson.keySet()) {
                            if (!tcyrSyncExcludeFieldList.contains(key)) {
                                extentJson.put(key, customJson.get(key));
                            }
                        }
                        extentJson.put("syncFileId", syncFileId);
                        sqlBuilder.append(",'").append(escapeSqlString(extentJson.toJSONString())).append("',1,NOW(),NOW())");
                    }else {
                        sqlBuilder.append(",'").append(line).append("',NULL,NULL,NULL");
                        JSONObject extentJson = new JSONObject();
                        extentJson.put("column_0", line);
                        sqlBuilder.append(",'").append(escapeSqlString(JSONObject.toJSONString(extentJson))).append("',0,NOW(),NOW())");
                    }
                    count++;
                }
                // 执行批量插入
                log.info("{}执行批量插入，apiCode:{}, batchNo:{}, count:{},sql:{}", TITLE, apiCode, batchNo, count,sqlBuilder.toString());
                tcyrSyncMapper.insertDataToDb(sqlBuilder.toString());
            }
        }catch (Exception e) {
            tcyrSyncFileMapper.updateDbDealStatus(syncFileId,3);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
    }

    /**
     * 转义 SQL 字符串中的特殊字符
     * @param str 需要转义的字符串
     * @return 转义后的字符串
     */
    private String escapeSqlString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''").replace("\\", "\\\\");
    }

    /**
     * 带重试机制的获取锁
     * @param lockKey 锁的key
     * @param lockValue 锁的值
     * @return 是否成功获取锁
     */
    private boolean acquireLockWithRetry(String lockKey, String lockValue) {
        int maxRetryTimes = marketingCommonConfig.getTcDbDealShardConfig().getInteger("lockRetryTimes");
        long retryIntervalMs = marketingCommonConfig.getTcDbDealShardConfig().getLong("lockRetryIntervalMs");
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

}

