package com.br.marketing.monkey.job.sanliuling;

import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.Impl.sanliuling.SanLiuLingCollectionService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @ClassName SanLiuLingCollectionJob
 * @Description 催收360上传数据清洗（营销）
 * https://c.100credit.cn/pages/viewpage.action?pageId=220971526
 * @Author kongbx
 * @Date 2025/9/12 16:12
 */
@Component
@Slf4j
public class SanLiuLingCollectionJob extends AbstractSimpleElasticJob {

    @Resource
    private JobManager jobManager;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private SanLiuLingCollectionService sanLiuLingCollectionService;
    private final static String TITLE = "【360-催收业务】";

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        log.warn(TITLE + "start");
        long start = System.currentTimeMillis();

        String sanLiuLingCollectionTime = marketingCommonConfig.getSanLiuLingCollectionTime();
        try {
            // 检查执行时间 HH:ss
            LocalTime currentTime = LocalTime.now();
            LocalTime executionTime = LocalTime.parse(sanLiuLingCollectionTime, DateTimeFormatter.ofPattern("HH:mm"));
            // 检查当前时间是否早于配置的执行时间
            boolean canExecute = currentTime.isBefore(executionTime);
            if (canExecute) {
                log.warn(TITLE + "当前时间未到执行时间，配置执行时间：{}，当前时间：{}",
                        sanLiuLingCollectionTime, currentTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                return;
            }
        }catch (Exception e){
            log.error(TITLE + "解析执行时间配置失败，配置值：{}", sanLiuLingCollectionTime, e);
            return;
        }

        String apiCode = shardingContext.getJobParameter();
        if (StringUtils.isEmpty(apiCode)) {
            apiCode = "3700317";
        }
        String actionDate = LocalDate.now().toString();
        int actionType = JobManager.ActionTypeEnum.SANLIULING_COLLECTION_DATA.getActionType();
        TransferActionFront action = jobManager.getFrontData(apiCode, actionDate, actionType, null);
        if (action != null) {
            Integer actionStatus = action.getStatus();
            log.warn(TITLE+"任务执行记录已存在, apiCode:{}, actionDate:{}, actionStatus:{}", apiCode, actionDate, actionStatus);
            return;
        }

        action = jobManager.saveFront(apiCode, actionDate, actionType);
        if (action.getId() == null) {
            log.warn(TITLE+ "任务执行记录新增失败, apiCode:{}, actionDate:{}", apiCode, actionDate);
            return;
        }

        sanLiuLingCollectionService.cleanData(apiCode);
        //执行完成
        jobManager.updateFrontDataStatus(action.getId(), 2);

        long end = System.currentTimeMillis();
        log.warn(TITLE + "end, 耗时{}ms", end - start);
    }

}
