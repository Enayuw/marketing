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
            ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(
                    marketingCommonConfig.getTcGzBatDBThreadPool(),
                    marketingCommonConfig.getTcGzBatDBThreadPool());
            while (true) {
                Integer searchSize = marketingCommonConfig.getTcPageSearchSize();
                actionPool.setCorePoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
                actionPool.setMaximumPoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
                List<MarketingTcyrSync> tcyrSyncList = tcSyncDataMatchService.selectUnMatchSyncList(apiCode,lastSearchId,searchSize);
                if (CollectionUtils.isEmpty(tcyrSyncList)) {
                    break;
                }
                //多线程 批量match
                tcSyncDataMatchService.matchTcyrSyncList(apiCode,tcyrSyncList);
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
