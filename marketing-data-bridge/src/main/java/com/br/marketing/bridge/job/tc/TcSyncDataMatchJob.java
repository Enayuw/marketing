package com.br.marketing.bridge.job.tc;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.service.tc.TcSyncDataMatchService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description 同程据匹配任务 - GZ数据拉取&&匹配入库
 * @Author zhiyong.zhang
 * @CreateTime 2025/04/21
 */
@Component
@Slf4j
public class TcSyncDataMatchJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程易融-Match任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcSyncDataMatchService tcSyncDataMatchService;



    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            log.warn(TITLE+"调度开始");

            Long lastSearchId = 0L;
            String apiCode = marketingCommonConfig.getTcyrApiCode();
            Integer searchSize = marketingCommonConfig.getTcPageSearchSize();
            ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(
                    marketingCommonConfig.getTcGzBatDBThreadPool(),
                    marketingCommonConfig.getTcGzBatDBThreadPool());
            while (true) {
                actionPool.setCorePoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
                actionPool.setMaximumPoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
                List<MarketingTcyrSync> tcyrSyncList = tcSyncDataMatchService.selectUnMatchSyncList(apiCode,lastSearchId,searchSize);
                if (CollectionUtils.isEmpty(tcyrSyncList)) {
                    break;
                }
                //多线程 批量match
                tcSyncDataMatchService.matchTcyrSyncList(apiCode,tcyrSyncList);
                // 多线程单个match
//                tcyrSyncList.forEach(tcyrSync ->
//                        actionPool.submit(() -> tcSyncDataMatchService.processUnMatchSingleData(apiCode, tcyrSync))
//                );
                lastSearchId = tcyrSyncList.get(tcyrSyncList.size() - 1).getId();
            }
            shutdownThreadPool(actionPool);
            log.warn(TITLE+"调度结束");
        }catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }


    public  void shutdownThreadPool(ThreadPoolExecutor executor) {
        log.warn(TITLE + "shutdownThreadPool开始");
        long taskCount = -1;
        executor.shutdown();
        try {
            while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                long completedTaskCount = executor.getCompletedTaskCount();
                if (taskCount == completedTaskCount) {
                    log.warn(TITLE + "业务线程等待超时");
                    break;
                }
                taskCount = completedTaskCount;
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
        } catch (Throwable e) {
            log.warn(TITLE + "ThreadPoolManager shutdown executor has error : ", e);
        }
        log.warn(TITLE + "shutdownThreadPool结束");
    }


}
