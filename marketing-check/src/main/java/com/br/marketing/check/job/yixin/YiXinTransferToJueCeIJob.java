package com.br.marketing.check.job.yixin;


import com.br.marketing.service.YiXinToJueCeProcessService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author 张广超
 * @Date 2023/6/16 17:16
 * @Description: 宜信转化数据推决策
 **/
@Component
@Slf4j
public class YiXinTransferToJueCeIJob extends AbstractSimpleElasticJob {

    private static final List<String> actonTypeList = new ArrayList<String>(){
        {
            add("A");
            add("B");
            add("C");
            add("D");
            add("E");
            add("F");
            add("G");
            add("H");
            add("I");
        }
    };
    @Resource
    private YiXinToJueCeProcessService yiXinToJueCeProcessService;
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        actonTypeList.forEach(e-> yiXinToJueCeProcessService.doProcess(e));
    }
}
