package com.br.marketing.monkey.job.taikang;

import com.br.marketing.monkey.job.taikang.util.SimpleDataPackToolsV2;
import com.br.marketing.service.Impl.tongcheng.TongChengOperationPushToCustomerService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 数据推送泰康
 *
 */
@Component
@Slf4j
public class TaiKangPushDataCustomerJob extends AbstractSimpleElasticJob {

    @Autowired
    SimpleDataPackToolsV2 simpleDataPackToolsV2;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cell","33333");
            simpleDataPackToolsV2.clientPacking("","",jsonObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }
}
