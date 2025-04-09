package com.br.marketing.monkey.job.qifu;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.service.Impl.qifu.QiFuQrySleepUserRealMessageService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;


/**
 * @ClassName QiFuQrySleepUserRealMessageJob
 * @Description 促动支用户信息3710139（营销→客户）
 * @Author kongbx
 * @Date 2024/6/25 11:25
 */
@Component
@Slf4j
public class QiFuQrySleepUserRealMessageJob extends AbstractSimpleElasticJob {

    @Autowired
    QiFuQrySleepUserRealMessageService service;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        String parameter = shardingContext.getJobParameter();
        String apiCode = "3710139";
        if (StringUtils.isNotEmpty(parameter)) {
            List<Map<String, String>> paramList = JSONObject.parseObject(parameter, List.class);
            for(Map<String, String> map : paramList){
                if(!StringUtils.isEmpty(map.get("apiCode"))){
                    apiCode = map.get("apiCode");
                }
            }
        }
        service.process(apiCode);
        long end = System.currentTimeMillis();
        log.warn("【奇富促动支用户信息】调度结束apiCodes:{}, 耗时:{}", apiCode, end - start);
    }

}
