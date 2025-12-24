package com.br.marketing.monkey.job.didi;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.didi.DiDiCollidingDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 * 滴滴短信流量数据撞库任务
 * <a href="https://c.100credit.cn/pages/viewpage.action?pageId=230971961">D20251210滴滴短信流量准入接口（sftp→api）-3710223</a>
 *
 * @author senyang.zheng
 * @since 2025/12/17
 */
@Component
@Slf4j
public class DidiV5CollidingJob extends AbstractSimpleElasticJob {

    @Resource
    private DiDiCollidingDataService diDiCollidingDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        List<Integer> shardingItems = context.getShardingItems();
        int shardingTotalCount = context.getShardingTotalCount();
        log.warn("滴滴短信流量数据撞库任务开始，总分片数：{}，当前分片：{}", shardingTotalCount, shardingItems);
        try {
            diDiCollidingDataService.colliding(context);
        } catch (Exception e) {
            String title = String.format("滴滴短信流量数据撞库任务，分片%s单次运行异常", shardingItems);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage(), title));
        }
        log.warn("滴滴短信流量数据撞库任务完成，分片{}运行耗时：{}s", shardingItems, (System.currentTimeMillis() - start) / 1000);
    }
}

