package com.br.marketing.service.Impl;

import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.yunke.YunKeClient;
import com.br.marketing.client.yunke.output.ChildDataZDto;
import com.br.marketing.client.yunke.output.YunKeResponseDto;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CellCollectRecordMapper;
import com.br.marketing.mapper.MarketingDeviceTypeMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.YunKeService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.SHAUtils;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author peng.kang
 * @description: 云客服务
 * @date 2025/5/24 10:03
 */
@Slf4j
@Service
public class yunKeImpl implements YunKeService {
    private static final String YUN_KE_REDIS_KEY = "YUNKEREDISKEY";
    private static final Integer PARTITION = 2000;
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    RedisChgService redisChgService;
    @Resource
    CellCollectRecordMapper cellCollectRecordMapper;
    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;
    @Resource
    MarketingDeviceTypeMapper marketingDeviceTypeMapper;
    @Resource
    YunKeClient yunKeClient;

    @Override
    public void phoneCollectByApiCode() {
        List<String> apiCodes = marketingCommonConfig.getCellOfApiCodeCellCollect();
        for (String apiCode : apiCodes) {
            String key = RedisKeyConstant.prefix.concat(YUN_KE_REDIS_KEY).concat(":").concat(apiCode);
            String lockValue = UUID.randomUUID().toString();
            try {
                redisChgService.lock(key, lockValue);
                //查询数据库(其它pod获取到锁继续判断该apiCode在本次任务中是否还没有被处理)
                CellCollectRecordExample example = new CellCollectRecordExample();
                example.createCriteria().andApiCodeEqualTo(apiCode);
                List<CellCollectRecord> cellCollectRecords = cellCollectRecordMapper.selectByExample(example);
                if (CollectionUtils.isNotEmpty(cellCollectRecords)) {
                    if (cellCollectRecords.get(0).getStatus() != 0) {
                        continue;
                    }
                } else {
                    initCellCollectRecord(apiCode);
                }
                //更新状态处理中
                cellCollectRecordMapper.updateStatusByApiCode(apiCode, 1);
                redisChgService.unlock(key, lockValue);
                //手机号收集
                CollectCellByApiCode(apiCode);
                //更新状态处理完成
                cellCollectRecordMapper.updateStatusByApiCode(apiCode, 2);
            } catch (Exception e) {
                redisChgService.unlock(key, lockValue);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YUNKE_SERVICEERROR.getCode(),
                        "云客手机号收集异常 apiCode:{}", apiCode));
                log.warn("云客手机号收集异常:{}", e.getMessage());
            }
        }
        //判断此轮任务是否全部完成 若全部完成 将状态置为初始0 给下一轮任务 放在此处集体更新是防止pod重复执行
        CellCollectRecordExample example = new CellCollectRecordExample();
        example.createCriteria().andStatusNotEqualTo(2);
        if (cellCollectRecordMapper.countByExample(example) == 0) {
            cellCollectRecordMapper.updateStatus(0);
        }
    }

    void initCellCollectRecord(String apiCode) {
        CellCollectRecord cellCollectRecord = new CellCollectRecord();
        cellCollectRecord.setApiCode(apiCode);
        cellCollectRecord.setUploadMaxId(0L);
        cellCollectRecord.setStatus(0);
        cellCollectRecord.setMaxId(0L);
        cellCollectRecord.setUpdateTime(new Date());
        cellCollectRecord.setCreateTime(new Date());
        cellCollectRecordMapper.insert(cellCollectRecord);
    }

    void CollectCellByApiCode(String apiCode) {
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(marketingCommonConfig.getCellCollectThreadNum(),
                marketingCommonConfig.getCellCollectThreadNum());
        while (true) {
            try {
                CellCollectRecordExample example = new CellCollectRecordExample();
                example.createCriteria().andApiCodeEqualTo(apiCode);
                List<CellCollectRecord> cellCollectRecords = cellCollectRecordMapper.selectByExample(example);
                List<MarketingSyncCell> marketingSyncCells = marketingSyncUserMapper.getCellByApiCodeAndMaxId(apiCode,
                        cellCollectRecords.get(0).getUploadMaxId(), marketingCommonConfig.getCellCollectPageSize());

                if (CollectionUtils.isEmpty(marketingSyncCells)) {
                    cellCollectRecordMapper.updateStatusByApiCode(apiCode, 2);
                    shutdownCollectCellThread(threadPool);
                    break;
                }
                //分组多线程处理
                getCellCollectThreadNum(threadPool);
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                List<List<MarketingSyncCell>> partitions = Lists.partition(marketingSyncCells, PARTITION);
                partitions.forEach(partition -> futures.add(CompletableFuture.runAsync(() ->
                        cellDataHandle(partition, apiCode), threadPool)));
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                //更新最大id
                CellCollectRecord record = new CellCollectRecord();
                record.setUploadMaxId(marketingSyncCells.get(marketingSyncCells.size() - 1).getId());
                record.setId(cellCollectRecords.get(0).getId());
                cellCollectRecordMapper.updateByPrimaryKeySelective(record);
            } catch (Exception e) {
                cellCollectRecordMapper.updateStatusByApiCode(apiCode, 2);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YUNKE_SERVICEERROR.getCode(),
                        "云客根据apiCode手机号收集异常!"));
                log.warn("云客根据apiCode手机号收集异常:{}", e.getMessage());
                shutdownCollectCellThread(threadPool);
                break;
            }
        }
    }

    private void getCellCollectThreadNum(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getCellCollectThreadNum();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
    }

    private void modifyDeviceTypeThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getDeviceTypeThreadNum();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
    }

    private void cellDataHandle(List<MarketingSyncCell> marketingSyncCells, String apiCode) {
        List<MarketingDeviceType> deviceTypes = new ArrayList<>();
        marketingSyncCells.forEach(marketingSyncCell -> {
            MarketingDeviceType deviceType = new MarketingDeviceType();
            String cellLogDecode = BrCipherMaker.getInstance().decode(marketingSyncCell.getCell());

            deviceType.setCell(marketingSyncCell.getCell());
            deviceType.setCellSha1(SHAUtils.encryptSHA1(cellLogDecode));
            deviceType.setCellMd5(marketingSyncCell.getCellMd5());
            deviceType.setCellSha256(marketingSyncCell.getCellSha256());
            deviceType.setApiCode(apiCode);
            deviceTypes.add(deviceType);
        });
        marketingDeviceTypeMapper.saveBatch(deviceTypes);
    }

    @Override
    public void getDeviceType() {
        Integer pageSize = marketingCommonConfig.getCollectDeviceTypePageSize();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(marketingCommonConfig.getDeviceTypeThreadNum(),
                marketingCommonConfig.getDeviceTypeThreadNum());
        if (marketingCommonConfig.getDeviceTypeExecuteCondition()) {
            //按照device_type is NULL查找增量数据
            incrementalDataProcessing(pageSize, threadPool);
        } else {
            //按照maxId更新device_type(三个月一次)
            historyDataProcessing(pageSize, threadPool);
        }
        shutdownDeviceTypeThread(threadPool);
    }

    void doProcess(List<MarketingDeviceType> deviceTypes) {
        List<String> checkData = new ArrayList<>(deviceTypes.size());
        deviceTypes.forEach(deviceType -> {
            checkData.add(deviceType.getCellSha1());
        });
        Result<YunKeResponseDto> result = yunKeClient.getYunKeDeviceType(checkData);
        if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
            List<ChildDataZDto> childData = result.getData().getData();
            if (CollectionUtils.isNotEmpty(childData)) {
                childData.forEach(data -> {
                    marketingDeviceTypeMapper.updateDeviceTypeBySha1(data.getCheckElement(), data.getParseState());
                });
            }
        }
    }

    void incrementalDataProcessing(Integer pageSize, ThreadPoolExecutor threadPool) {
        while (true) {
            try {
                List<MarketingDeviceType> list = marketingDeviceTypeMapper.selectPageByDeviceType(pageSize);
                if (CollectionUtils.isEmpty(list)) {
                    break;
                }
                modifyDeviceTypeThreadPool(threadPool);
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                List<List<MarketingDeviceType>> partitions = Lists.partition(list,
                        marketingCommonConfig.getDeviceTypePartitionNum());
                partitions.forEach(partition -> futures.add(CompletableFuture.runAsync(()
                        -> doProcess(partition), threadPool)));
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YUNKE_SERVICEERROR.getCode(),
                        "云客增量数据刷机异常!"));
                log.warn("云客增量数据刷机异常:{}", e.getMessage());
            }
        }
    }

    void historyDataProcessing(Integer pageSize, ThreadPoolExecutor threadPool) {
        List<String> apiCodes = marketingCommonConfig.getCellOfApiCodeCellCollect();
        apiCodes.forEach(apiCode -> {
            try {
                while (true) {
                    CellCollectRecordExample example = new CellCollectRecordExample();
                    example.createCriteria().andApiCodeEqualTo(apiCode);
                    List<CellCollectRecord> cellCollectRecords = cellCollectRecordMapper.selectByExample(example);
                    if (CollectionUtils.isEmpty(cellCollectRecords)) {
                        break;
                    }
                    List<MarketingDeviceType> list = marketingDeviceTypeMapper.selectPageByCodeMaxId(pageSize, apiCode,
                            cellCollectRecords.get(0).getMaxId());
                    if (CollectionUtils.isEmpty(list)) {
                        //一个apiCode结束 将maxId更新成0 下轮任务从0开始继续刷新机型
                        cellCollectRecordMapper.updateMaxIdByApiCode(apiCode, 0L);
                        break;
                    }
                    modifyDeviceTypeThreadPool(threadPool);
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    List<List<MarketingDeviceType>> partitions = Lists.partition(list,
                            marketingCommonConfig.getDeviceTypePartitionNum());
                    partitions.forEach(partition -> futures.add(CompletableFuture.runAsync(()
                            -> doProcess(partition), threadPool)));
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                    //一批次结束 更新最大maxId
                    cellCollectRecordMapper.updateMaxIdByApiCode(apiCode, list.get(list.size() - 1).getId());
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YUNKE_SERVICEERROR.getCode(),
                        "云客历史数据刷机异常!"));
                log.warn("云客历史数据刷机异常:{}", e.getMessage());
            }
        });
    }

    private static void shutdownDeviceTypeThread(ThreadPoolExecutor deviceTypeThread) {
        deviceTypeThread.shutdown();
        try {
            while (!deviceTypeThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("云客机型获取任务等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private static void shutdownCollectCellThread(ThreadPoolExecutor collectCellThread) {
        collectCellThread.shutdown();
        try {
            while (!collectCellThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("云客手机号收集任务等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
