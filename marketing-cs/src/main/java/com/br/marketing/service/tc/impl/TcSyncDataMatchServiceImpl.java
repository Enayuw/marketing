package com.br.marketing.service.tc.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.mapper.MarketingTcyrSyncMapper;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.tc.TcSyncDataMatchService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 同城易融拉取文件入库-Service实现
 *
 * @author zhiyong.zhang
 * @date 2024/04/21
 */
@Service
@Slf4j
public class TcSyncDataMatchServiceImpl implements TcSyncDataMatchService {

    private static final String TITLE = "【同城易融上传数据匹配】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;


    @Resource
    private MarketingTcyrSyncMapper tcyrSyncMapper;


    @Autowired
    RedisChgService redisChgService;

    @Override
    public List<MarketingTcyrSync> selectUnMatchSyncList(String apiCode,Long lastSearchId, Integer searchSize) {
        return tcyrSyncMapper.selectUnMatchSyncList(apiCode,lastSearchId,searchSize);
    }

    @Override
    public void matchTcyrSyncList(String apiCode,List<MarketingTcyrSync> tcyrSyncList) {
        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(10, 10);
        List<CompletableFuture<Result>> futureList = new ArrayList<>();
        List<Long> resultList = Collections.synchronizedList(new ArrayList<>(20));

        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(actionPool, marketingCommonConfig.getTcGzBatDBThreadPool());
        futureList.add(CompletableFuture.supplyAsync(() -> processUnMatchData(apiCode,tcyrSyncList,marketingCommonConfig.getTcPartSize()), actionPool)
                .whenComplete((processDataResult, throwable) -> {
                    if (processDataResult == null || !processDataResult.isSuccess()) {
                        resultList.add(0L);
                        return;
                    }
                    resultList.add((Long) processDataResult.getData());
                    if (throwable != null) {
                        log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),throwable.getMessage(), TITLE), throwable);
                        resultList.add(0L);
                    }
                })
        );
    }

    @Override
    public void processUnMatchSingleData(String apiCode, MarketingTcyrSync tcyrSync) {
        try {
            tcyrSync.setIsMatch(0);
            tcyrSync.setIsClean(0);
            String cell = tcyrSyncRecordMapper.selectSingleLastCustNumCelltikv_(apiCode,tcyrSync.getUserKey());
            if (StringUtils.isNotBlank(cell)) {
                tcyrSync.setCell(cell);
                tcyrSync.setIsMatch(1);
            }
            tcyrSyncMapper.updateMatchInfo(tcyrSync);
        }catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    private Result processUnMatchData(String apiCode,List<MarketingTcyrSync> tcyrSyncList,Integer partSize) {
        Result result = new Result().failure();
        try {
            //is_match 默认设置0，匹配中修改为1
            tcyrSyncList.forEach(tcyrSync -> {tcyrSync.setIsMatch(0);});

            List<String> userKeyList = tcyrSyncList.stream().map(MarketingTcyrSync::getUserKey).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(userKeyList)) {
                return  result.success();
            }
            List<Map<String, String>> userCellMap = tcyrSyncRecordMapper.selectLastCustNumCelltikv_(apiCode,userKeyList);
            Map<String, String> resultMap = new HashMap<>();
            for (Map<String, String> map : userCellMap) {
                String custNum = map.get("custNum");
                String cell = map.get("cell");
                if (StringUtils.isNotBlank(custNum) && StringUtils.isNotBlank(cell)) {
                    resultMap.put(custNum, cell);
                }
            }
            for (MarketingTcyrSync syncItem : tcyrSyncList) {
                if (resultMap.containsKey(syncItem.getUserKey())) {
                    syncItem.setCell(resultMap.get(syncItem.getUserKey()));
                    syncItem.setIsMatch(1);
                    syncItem.setIsClean(0);
                }
            }
            List<List<MarketingTcyrSync>> partitionList = ListUtils.partition(tcyrSyncList, partSize);
            for (List<MarketingTcyrSync> partitionItemList : partitionList) {
                tcyrSyncMapper.batchUpdateMatchInfo(partitionItemList);
            }
            return result.success();
        } catch(Exception e){
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
            return result.failure();
        }
    }


    @Override
    public void shardProcess(String apiCode) {
        String lockKey = RedisKeyConstant.tcyrSyncMatch.concat(apiCode);
        String lockValue = "";
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYC_MATCH.getName(), 100, 100);
        DateTimeFormatter formatter= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startSearchTime = LocalDateTime.parse(
                marketingCommonConfig.getTcMatchShardConfig().getString("startSearchTime"), formatter);
        try{
            for (;;) {
                if (!marketingCommonConfig.getTcMatchShardConfig().getBoolean("jobSwitch")) {
                    break;
                }
                lockValue = UUID.randomUUID().toString();
                long startTime = System.currentTimeMillis();
                //1.抢锁
                redisChgService.lock(lockKey, lockValue);

                //2.获取数据
                List<MarketingTcyrSync> tcyrSyncList = tcyrSyncMapper.selectMatchSyncList(
                        apiCode, marketingCommonConfig.getTcMatchShardConfig().getInteger("pageSize"),startSearchTime);
                if (CollectionUtils.isEmpty(tcyrSyncList)) {
                    redisChgService.unlock(lockKey, lockValue);
                    break;
                }
                //3.修改中间状态
                dealMiddleState(tcyrSyncList);
                //4.释放锁
                redisChgService.unlock(lockKey, lockValue);
                log.warn("TITLE:{},shardMatch抢锁->释放锁耗时:{}",TITLE,System.currentTimeMillis()-startTime);
                //5.多线程单个处理匹配
                shardMathTcyrSynList(apiCode, tcyrSyncList, actionPool);
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }finally {
            redisChgService.unlock(lockKey, lockValue);
            actionPool.shutdownAndAwaitTermination();
        }
    }


    private void shardMathTcyrSynList(String apiCode, List<MarketingTcyrSync> tcyrSyncList,ThreadPoolExecutor actionPool) {
        if(marketingCommonConfig.getTcMatchShardConfig().getBoolean("batchSwitch")){
            CompletableFuture.supplyAsync(() -> processUnMatchData(apiCode,tcyrSyncList,
                    marketingCommonConfig.getTcMatchShardConfig().getInteger("partSize")), actionPool);
        }else {
            tcyrSyncList.forEach(tcyrSync -> {
                CompletableFuture.runAsync(() -> processUnMatchSingleData(apiCode, tcyrSync), actionPool);
            });
        }
    }

    private void dealMiddleState(List<MarketingTcyrSync> tcyrSyncList) {
        List<Long> idList = tcyrSyncList.stream().map(MarketingTcyrSync::getId).collect(Collectors.toList());
        tcyrSyncMapper.updateMiddleMatchStatus(idList);
    }

}
