package com.br.marketing.service.tc.impl;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.tc.TcServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrSyncMapper;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.tc.TcSyncDataMatchService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    private static final String TITLE = "【同城易融上传数据匹配-gz包拉取入库】";

    private Integer PARTITION_SIZE = 1000;

    @Resource
    private TcServiceClient tcServiceClient;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;


    @Resource
    private MarketingTcyrSyncMapper tcyrSyncMapper;



    @Override
    public Integer updageTcyrRecordSyncStatus(String batchNo, Integer status) {
        return tcyrSyncRecordMapper.updageTcyrRecordSyncStatus(batchNo,status);
    }

    @Override
    public Long selectWaitMatchCount(String apiCode) {
        return tcyrSyncMapper.selectWaitMatchCount(apiCode);
    }

    @Override
    public Integer dealTcMatch(String apiCode) {
        return tcyrSyncMapper.dealTcMatch(apiCode);
    }

    @Override
    public List<MarketingTcyrSync> selectUnMatchSyncList(String apiCode,Long lastSearchId, Integer searchSize) {
        return tcyrSyncMapper.selectUnMatchSyncList(apiCode,lastSearchId,searchSize);
    }

    @Override
    public void matchTcyrSyncList(String apiCode,List<MarketingTcyrSync> tcyrSyncList) {
        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(10, 10);
        List<CompletableFuture<Result>> futureList = new ArrayList<>();
        List<Long> resultList = Collections.synchronizedList(new ArrayList<>(20));

        actionPool.setCorePoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
        actionPool.setMaximumPoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
        futureList.add(CompletableFuture.supplyAsync(() -> processUnMatchData(apiCode,tcyrSyncList), actionPool)
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

    private Result processUnMatchData(String apiCode,List<MarketingTcyrSync> tcyrSyncList) {
        Result result = new Result().failure();
        try {
            //is_match 默认设置0，匹配中修改为1
            tcyrSyncList.forEach(tcyrSync -> {tcyrSync.setIsMatch(0);});

            List<String> userKeyList = tcyrSyncList.stream().map(MarketingTcyrSync::getUserKey).collect(Collectors.toList());
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
            tcyrSyncMapper.batchUpdateMatchInfo(tcyrSyncList);
            return result.success();
        } catch(Exception e){
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
            return result.failure();
        }
    }
}
