package com.br.marketing.check.job.yixin;


import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.IYiXinTransferService;
import com.br.marketing.service.YiXinToJueCeProcessService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author 张广超
 * @Date 2023/6/16 17:16
 * @Description: 宜信转化数据推决策
 **/
@Component
@Slf4j
public class YiXinTransferToJueCeIJob extends AbstractSimpleElasticJob {

    @Resource
    private YiXinToJueCeProcessService yiXinToJueCeProcessService;
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        yiXinToJueCeProcessService.doProcess_A();
        yiXinToJueCeProcessService.doProcess_B();
        yiXinToJueCeProcessService.doProcess_C();
        yiXinToJueCeProcessService.doProcess_D();
        yiXinToJueCeProcessService.doProcess_E();
        yiXinToJueCeProcessService.doProcess_F();
        yiXinToJueCeProcessService.doProcess_G();
        yiXinToJueCeProcessService.doProcess_H();
        yiXinToJueCeProcessService.doProcess_I();

    }
}
