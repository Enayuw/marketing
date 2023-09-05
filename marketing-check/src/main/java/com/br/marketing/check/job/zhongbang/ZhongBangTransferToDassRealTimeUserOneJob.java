package com.br.marketing.check.job.zhongbang;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.service.Impl.zhongbang.ZhongBangService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * D20230816众邦自动化转Daas（营销→Daas）-3710094（api→api） http://c.100credit.cn/pages/viewpage.action?pageId=125078844
 * 众邦转化数据定时推送人工实时推送用户名单(单条)
 *
 * @author zeqiang.guo
 * @dateTime 2023/08/24 17:13
 */
@Component
@Slf4j
public class ZhongBangTransferToDassRealTimeUserOneJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongBangService zhongBangService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        /* param 格式：apiCode或{"apiCode":"yyyy-MM-dd或yyyy-MM-ddTHH:mm:ss,yyyy-MM-ddTHH:mm:ss"}
         * eg1：7410994
         * 或
         * eg2：{"7410994":"2023-08-25"}
         * 或
         * eg2：{"7410994":"2023-08-25T00:00:00,2023-08-25T23:59:59"}
         */
        String parameter = context.getJobParameter();
        JSONObject jsonObject = new JSONObject();
        int poolSize = marketingCommonConfig.getZhongBangTransferPushDaasThreadPoolSize();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(poolSize, poolSize, 5);
        try {
            if (StringUtils.isBlank(parameter)) {
                jsonObject.put("3710094", LocalDate.now().toString());

            } else {
                try {
                    jsonObject = JSONObject.parseObject(parameter);
                } catch (Exception e) {
                    jsonObject.put(parameter, LocalDate.now().toString());
                    log.warn(e.getMessage(), e);
                }
            }
            jsonObject.forEach((k, v) -> {
                String[] dateTimeStr = v.toString().split(",");
                zhongBangService.pushTransferToDaasRealTimeUserOneAndCustomer(k, threadPool, dateTimeStr);

            });
            threadPool.shutdown();
            long taskCount = -1;
            long timeout = 10;
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                long completedTask2Count = threadPool.getCompletedTaskCount();
                if (taskCount == completedTask2Count) {
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            threadPool.shutdownNow();
        }
        long end = System.currentTimeMillis();
        log.warn("众邦自动化转Daas任务结束，参数信息:{}，运行耗时:{}"
                , jsonObject, end - start);
    }
}

