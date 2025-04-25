package com.br.marketing.bridge.job.tc;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.service.tc.TcSyncDataMatchService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

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
            atciton(marketingCommonConfig.getTcyrApiCode());
            log.warn(TITLE+"调度结束");
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    /**
     * 多线程match
     * @param apiCode
     */
    private void atciton(String apiCode) {
        Long waitMatchCount = tcSyncDataMatchService.selectWaitMatchCount(apiCode);
        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(10, 10);
        boolean stillFlag = waitMatchCount>0L;
        while (stillFlag) {
            processList(apiCode,actionPool);
            waitMatchCount = tcSyncDataMatchService.selectWaitMatchCount(apiCode);
            stillFlag = waitMatchCount>0L;
        }

    }

    private Result processList(String apiCode, ThreadPoolExecutor actionPool) {
        Result result = new Result().failure();
        actionPool.setCorePoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
        actionPool.setMaximumPoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
        CompletableFuture.supplyAsync(() -> processData(apiCode), actionPool)
                .whenComplete((processDataResult, throwable) -> {
                    if (throwable != null) {
                        log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),throwable.getMessage(), TITLE), throwable);
                    }
                });
        return result.success();
    }

    private Result processData(String apiCode) {
        Result result = new Result().failure();
        try {
            tcSyncDataMatchService.dealTcMatch(apiCode);
            return result.success();
        } catch (Exception e) {
            log.error(TITLE + "processData error", e);
            return result.failure();
        }
    }


}
