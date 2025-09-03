package com.br.marketing.monkey.job.halo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.service.Impl.halo.IHaloCallbackService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 【紧急】D20250901哈啰硅基人数据回传-3710212（营销→客户）
 * https://c.100credit.cn/pages/viewpage.action?pageId=220958924
 *
 * @author Hua Qiang
 * @date 2024-10-29 17:53
 */
@Component
@Slf4j
public class HaloCallbackJob extends AbstractSimpleElasticJob {

    @Resource
    private IHaloCallbackService haloCallbackService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();

        JSONObject param = JSON.parseObject(context.getJobParameter());
        String batchNumber = param.getString("batchNumber");
        LocalDate date = Optional.ofNullable(param.getString("date"))
                .map(dateStr -> LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd")))
                .orElseGet(LocalDate::now);
        String whereSql = Optional.ofNullable(param.getString("whereSql")).orElse("");
        log.warn("哈啰硅基人数据回传调度开始 batchNumber:{},处理文件的日期：{}", batchNumber, date);
        haloCallbackService.pushDataCallback(batchNumber, date, whereSql);
        long end = System.currentTimeMillis();
        log.warn("哈啰硅基人数据回传调度结束, 耗时:{}", end - start);
    }
}
