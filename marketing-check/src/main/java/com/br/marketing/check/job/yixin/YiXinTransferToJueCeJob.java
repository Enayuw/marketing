package com.br.marketing.check.job.yixin;


import IceInternal.Ex;
import com.br.marketing.service.YiXinToJueCeProcessService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.TreeMap;


/**
 * @Author 张广超
 * @Date 2023/6/16 17:16
 * @Description: 宜信转化数据推决策
 **/
@Component
@Slf4j
public class YiXinTransferToJueCeJob extends AbstractSimpleElasticJob {
    private static final TreeMap<String, String> ACTONTYPETREE = new TreeMap<>();

    static {
        ACTONTYPETREE.put("A", null);
        ACTONTYPETREE.put("B", "12");
        ACTONTYPETREE.put("C", "13");
        ACTONTYPETREE.put("D", "23");
        ACTONTYPETREE.put("E", "20");
        ACTONTYPETREE.put("F", "21");
        ACTONTYPETREE.put("G", "8");
        ACTONTYPETREE.put("H", "15");
        ACTONTYPETREE.put("I", "6");
    }

    @Resource
    private YiXinToJueCeProcessService yiXinToJueCeProcessService;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            yiXinToJueCeProcessService.doProcess(ACTONTYPETREE);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }

    }




}

