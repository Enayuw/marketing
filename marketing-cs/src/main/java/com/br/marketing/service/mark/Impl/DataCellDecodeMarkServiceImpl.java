package com.br.marketing.service.mark.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.br.common.encryption.Sha256Util;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.FlagData;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.mark.DataCellDecodeMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * -----------------------------
 * PackageName： com.br.marketing.service.mark.Impl
 * ClassName：DataWhiteListMarkServiceImpl
 * Description：白名单实现类
 *
 * @author：it-yml CreateTime：2025-02-21
 * -----------------------------
 */
@Service
@Slf4j
public class DataCellDecodeMarkServiceImpl implements DataCellDecodeMarkService {

    private final static int PARTATION_SIZE = 2000;
    @Resource
    FlagDataMapper flagDataMapper;
    @Resource
    RedisChgService redisChgService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;


    @Override
    public void process() {

        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            Integer pageSize = marketingCommonConfig.getDataMarkPageSize();
            Integer threadPoolSize = marketingCommonConfig.getDataMarkThreadNum();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
            String key = RedisKeyConstant.DATA_CELL_DECODE_LIST_MARK.concat(":").concat(apiCode);
            while (true) {
                String lockValue = UUID.randomUUID().toString();
                try {
                    redisChgService.lock(key, lockValue);
                    //打标表数据查询
                    List<FlagData> list = flagDataMapper.queryCellListComputation(pageSize, apiCode);
                    if (CollectionUtil.isEmpty(list)) {
                        redisChgService.unlock(key, lockValue);
                        threadPoolShutDown(threadPool);
                        break;
                    }
                    List<Long> ids = list.stream().map(FlagData::getId).collect(Collectors.toList());
                    flagDataMapper.batchUpdateCellDecodeListComputationByIds(ids, 0);
                    //释放锁
                    try {
                        redisChgService.unlock(key, lockValue);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                                "pp停车加解密释放锁出现异常，" + "errorMessage=" + e.getMessage()), e);
                        redisChgService.unlock(key, lockValue);
                        threadPoolShutDown(threadPool);
                        break;
                    }
                    //打标更新:FlagWhiteList
                    updateCellDecodeList(threadPool, list);
                    threadPoolShutDown(threadPool);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                            "pp停车加解密抢锁出现异常，" + "errorMessage=" + e.getMessage()), e);
                    redisChgService.unlock(key, lockValue);
                    threadPoolShutDown(threadPool);
                    break;
                }
            }
        });
    }

    void updateCellDecodeList(ThreadPoolExecutor threadPool, List<FlagData> ids) {
        List<List<FlagData>> partitions = Lists.partition(ids, PARTATION_SIZE);
        for (List<FlagData> partition : partitions) {
            threadPool.submit(() -> markAndUpdateCellDecodeList(partition));
        }
    }

    void markAndUpdateCellDecodeList(List<FlagData> list) {

        try {
            for (FlagData flagData : list) {
                String decodeCell = RpcClientProxy.decode(flagData.getCellMd5(), "cell", "md5", "");
                flagData.setCellLog(BrCipherMaker.getInstance().encode(decodeCell));
                flagData.setCellSha256(Sha256Util.getSHA256Encrypt(decodeCell));
                flagData.setFlagCellDecodeComputation(1);
                flagDataMapper.batchUpdateCellDecodeListByIds(flagData);
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    "pp停车加解密异常" + "errorMessage=" + e.getMessage()), e);
        }

    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("pp停车白名单线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    "pp停车白名单线程作业，日志保存线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
    }
}
