package com.br.marketing.check.job.yixin;


import com.br.marketing.service.YiXinToJueCeProcessService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashMap;


/**
 * @Author 张广超
 * @Date 2023/6/16 17:16
 * @Description: 宜信转化数据推决策
 **/
@Component
@Slf4j
public class YiXinTransferToJueCeJob extends AbstractSimpleElasticJob {
    private static final LinkedHashMap<String, String> ACTONTYPELINK = new LinkedHashMap<>();

    static {
        ACTONTYPELINK.put("A", null);
        ACTONTYPELINK.put("B", "12");
        ACTONTYPELINK.put("C", "13");
        ACTONTYPELINK.put("D", "23");
        ACTONTYPELINK.put("E", "20");
        ACTONTYPELINK.put("F", "21");
        ACTONTYPELINK.put("G", "8");
        ACTONTYPELINK.put("H", "15");
        ACTONTYPELINK.put("I", "6");
        ACTONTYPELINK.put("J", "13");
        ACTONTYPELINK.put("K", "13");
        ACTONTYPELINK.put("L", null);
    }

    @Resource
    private YiXinToJueCeProcessService yiXinToJueCeProcessService;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            yiXinToJueCeProcessService.doProcess(ACTONTYPELINK);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }

    }




}

