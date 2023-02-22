package com.br.marketing.check.job.ppdai;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.IPPDTransferService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

/**
 * 拍拍贷老客周期性推送电销
 * <p>
 * 推送的数据为转化数据，数据源为推送电销的记录表
 *
 * @author Guo Zeqiang
 * @dateTime 2023-02-13 10:35
 */
@Component
@Slf4j
public class PPDaiOldPeriodicityPushDxJob extends AbstractSimpleElasticJob {

    @Resource
    private IPPDTransferService ippdTransferService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        String jobParameter = context.getJobParameter();
        Map<Integer, String> shardingItemParameters = context.getShardingItemParameters();
        LocalDate now;
        if (CollectionUtils.isEmpty(shardingItemParameters)) {
            now = LocalDate.now();
        } else {
            now = LocalDate.parse(shardingItemParameters.get(0), DateTimeFormatter.ISO_LOCAL_DATE);
        }
        long number;
        String[] apiCodes = new String[]{"3710015"};
        if (StringUtils.isNotBlank(jobParameter)) {
            apiCodes = jobParameter.split(",");
            number = ippdTransferService.ppdaiOldPeriodicityPushDx(now, apiCodes);
        } else {
            number = ippdTransferService.ppdaiOldPeriodicityPushDx(now, apiCodes);
        }
        long end = System.currentTimeMillis();
        log.warn("拍拍贷老客周期性推电销任务结束，apiCede:{},数据总量={}，运行耗时:{}"
                , Arrays.toString(apiCodes), number, end - start);
    }
}
