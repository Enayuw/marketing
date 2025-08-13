package com.br.marketing.service.tccpa.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTcyrCpaSuccessFile;
import com.br.marketing.entity.MarketingTcyrCpaSuccessRecord;
import com.br.marketing.enums.TcCpaCollidingDealStatusEnum;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessDataMapper;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessFileMapper;
import com.br.marketing.mapper.MarketingTcyrCpaSuccessRecordMapper;
import com.br.marketing.service.tccpa.TcCpaCollidingDealService;
import com.br.marketing.service.tccpa.TcCpaCustCellMappingService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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

    @Autowired
    private RedisChgService redisChgService;

    @Override
    public void shardProcess(String apiCode) {
        String lockKey = RedisKeyConstant.tcyrCpaDbDeal.concat(apiCode);;
        String lockValue = UUID.randomUUID().toString();
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_CPA_COLLIDING_DEAL.getName(), 50, 50);
        try {
            for (;;) {
                if (!marketingCommonConfig.getTcCpaDbDealShardConfig().getBoolean("jobSwitch")) {
                    break;
                }
                //1.抢锁 - 添加重试机制
                boolean lockAcquired = acquireLockWithRetry(lockKey, lockValue);
                if (!lockAcquired) {
                    log.warn("{}获取锁失败，apiCode:{}，跳过本次处理", TITLE, apiCode);
                    continue;
                }
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
            while ((line = reader.readLine()) != null) {
                batchData.add(line);
                if (batchData.size() == marketingCommonConfig.getTcCpaDbDealShardConfig().getInteger("pageSize")) {
                    if (!marketingCommonConfig.getTcCpaDbDealShardConfig().getBoolean("jobSwitch")) {
                        batchData.clear();
                        break;
                    }
                    List<String> batchDealData = new ArrayList<>(batchData);
                    actionPool.submit(()->
                            dbDealBatchLine(tcyrCpaSuccessFile.getApiCode(),tcyrCpaSuccessRecord.getBatchNo(),tcyrCpaSuccessRecord.getData(),tcyrCpaSuccessFile.getId(),batchDealData)
                    );
                    batchData.clear();
                }
                totalCount++;
            }
            if (!batchData.isEmpty()) {
                actionPool.submit(()->
                        dbDealBatchLine(tcyrCpaSuccessFile.getApiCode(),tcyrCpaSuccessRecord.getBatchNo(),tcyrCpaSuccessRecord.getData(),tcyrCpaSuccessFile.getId(),batchData)
                );
            }
            //3.修改csvFile totalCount数量、dbDeal状态、
            tcyrCpaSuccessFileMapper.updateColliDingDataDealStatusAndTotalCount(tcyrCpaSuccessFile.getId(),TcCpaCollidingDealStatusEnum.DEAL_SUCCESS.getValue(), totalCount);
        } catch (IOException e) {
            //4.修改quick_deal_status 异常状态
            tcyrCpaSuccessFileMapper.updateColliDingDataDealStatus(tcyrCpaSuccessFile.getId(),TcCpaCollidingDealStatusEnum.DEAL_FAIL.getValue());
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
        log.warn("TITLE:{},sync_file_id:{} db_deal执行结束,耗时:{}", TITLE, tcyrCpaSuccessFile.getId(), System.currentTimeMillis() - startTime);
    }

    private void dbDealBatchLine(String apiCode, String batchNo, String customerData, Long syncFileId, List<String> batchData) {
        JSONObject customJson = JSONObject.parseObject(customerData);
        try {
            String startDateStr = customJson.getString("startDate");
            String endDateStr = customJson.getString("endDate");
            List<List<String>> partitionList = ListUtils.partition(
                    batchData, marketingCommonConfig.getTcCpaDbDealShardConfig().getInteger("dbPartSize"));
            for (List<String> partitionItemList : partitionList) {
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("INSERT INTO b_tcyr_cpa_success_data (api_code,batch_no,sync_file_id,user_key," +
                        "cell,is_match,extend,status,create_time,update_time,start_date,end_date) VALUES");
                int count = 0;
                for (String line : partitionItemList) {
                    if (count > 0) {
                        sqlBuilder.append(",");
                    }
                    String[] data = line.split(",");
                    sqlBuilder.append("('").append(escapeSqlString(apiCode)).
                            append("','").append(escapeSqlString(batchNo)).append("',").append(syncFileId);
                    if (data.length == 0) {
                        sqlBuilder.append(",'").append(line).append("',NULL,NULL,NULL");
                        JSONObject extentJson = new JSONObject();
                        extentJson.put("column_0", line);
                        sqlBuilder.append(",'").append(escapeSqlString(JSONObject.toJSONString(extentJson))).append("',0,NOW(),NOW(),'")
                                .append(startDateStr).append("','").append(endDateStr).append("')");
                    } else if (data.length >= 1) {
                        String userKey = data[0].trim();
                        sqlBuilder.append(",'").append(escapeSqlString(userKey)).append("'");
                        //cell is_match 查中间表数据(不从原始上传明细查询补充中间表)
                        String cell = custCellMappingService.selectCell(userKey);
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
                        List<String> tcyrSyncExcludeFieldList = marketingCommonConfig.getTcyrCpaSyncSaveExcludeFieldList();
                        for (String key : customJson.keySet()) {
                            if (!tcyrSyncExcludeFieldList.contains(key)) {
                                extentJson.put(key, customJson.get(key));
                            }
                        }
                        extentJson.put("syncFileId", syncFileId);
                        sqlBuilder.append(",'").append(escapeSqlString(extentJson.toJSONString())).append("',1,NOW(),NOW(),'")
                                .append(startDateStr).append("','").append(endDateStr).append("')");
                    }
                    count++;
                }
                // 执行批量插入
                log.info("{}执行批量插入，apiCode:{}, batchNo:{}, count:{},sql:{}", TITLE, apiCode, batchNo, count,sqlBuilder.toString());
                tcyrCpaSuccessDataMapper.insertDataToDb(sqlBuilder.toString());
            }
        }catch (Exception e) {
            tcyrCpaSuccessFileMapper.updateColliDingDataDealStatus(syncFileId,3);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
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

    private Boolean isLong(String userKey) {
        try {
            Long.parseLong(userKey);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    /**
     * 带重试机制的获取锁
     * @param lockKey 锁的key
     * @param lockValue 锁的值
     * @return 是否成功获取锁
     */
    private boolean acquireLockWithRetry(String lockKey, String lockValue) {
        int maxRetryTimes = 3;
        long retryIntervalMs = 3;
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
