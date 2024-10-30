package com.br.marketing.monkey.job.guomei;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.service.Impl.guomei.IGuoMeiDataCallbackService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;

/**
 * 【紧急】D20241028国美用户数据回传-3710076（营销→客户）
 * https://c.100credit.cn/pages/viewpage.action?pageId=184397519
 *
 * @author Hua Qiang
 * @date 2024-10-29 17:53
 */
@Component
@Slf4j
public class GuoMeiDataCallbackJob extends AbstractSimpleElasticJob {

    @Resource
    private IGuoMeiDataCallbackService guoMeiDataCallbackService;


    /**
     * 2024-10-30 0:54
     * JobParameter: {"3710076":"2024-10-30"}
     */
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String apiCode = "3710076";
        long start = System.currentTimeMillis();
        String parameter = context.getJobParameter();
        if (JSON.isValidObject(parameter)) {
            JSONObject jsonObject = JSON.parseObject(parameter);
            jsonObject.forEach((k, v) -> {
                LocalDate parse = LocalDate.parse(v.toString());
                log.warn("国美用户数据回传调度开始apiCodes:{},处理文件的日期：{}", apiCode, parse);
                guoMeiDataCallbackService.pushDataCallback(k, parse);
            });
        } else {
            LocalDate now = LocalDate.now();
            log.warn("国美用户数据回传调度开始apiCodes:{},处理文件的日期：{}", apiCode, now);
            guoMeiDataCallbackService.pushDataCallback(apiCode, now);
        }
        long end = System.currentTimeMillis();
        log.warn("国美用户数据回传调度结束, 耗时:{}", end - start);
    }
}
