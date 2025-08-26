package com.br.marketing.monkey.job.test;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.test.MockTestService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @ClassName MockTestJob
 * @Description 测试mock注解性能
 * parameter格式：{apiCode:"",appletDate:""}
 * @Author kongbx
 * @Date 2025/8/26 10:27
 */
@Component
@Slf4j
public class MockTestJob extends AbstractSimpleElasticJob {

    @Autowired
    private MockTestService mockTestService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

        //log.warn(TITLE + "start");
        String parameter = jobExecutionMultipleShardingContext.getJobParameter();

        JSONObject jsonObject = JSONObject.parseObject(parameter);
        String apiCode = jsonObject.getString("apiCode");
        String appletDate = jsonObject.getString("appletDate");

        if(StringUtils.isEmpty(apiCode) || StringUtils.isEmpty(appletDate)){
            log.warn("job参数不能为空,parameter:" + parameter);
            return;
        }

        Boolean aSwitch = marketingCommonConfig.getMockPerformanceSwitch();
        if(aSwitch){
            mockTestService.processNote(apiCode,appletDate);
        }else {
            mockTestService.process(apiCode,appletDate);
        }


    }

}
