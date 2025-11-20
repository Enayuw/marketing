package com.br.marketing.monkey.job.taikang;

import com.br.marketing.api.customer.upload.service.weiju.util.AESUtil;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.monkey.job.taikang.util.ChannelRequest;
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
import java.util.Map;

/**
 * 数据推送泰康
 *
 */
@Component
@Slf4j
public class TaiKangPushDataCustomerJob extends AbstractSimpleElasticJob {

    @Autowired
    SimpleDataPackToolsV2 simpleDataPackToolsV2;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
            JSONObject jsonObject = new JSONObject();
            HttpProxyClient httpProxyClient = new HttpProxyClient();
            try {
                Map<String, String> taiKangConfig = marketingCommonConfig.getTaiKangConfig();
                String s = AESUtil.encryptAES("nX7zCFT1HaUllNbM", "15122334455");
                jsonObject.put("applicantPhone", s);
                jsonObject.put("eventId", "channel_browse_interruption");
                jsonObject.put("channelCode", "RongDa");
                jsonObject.put("applicantName", "");
                jsonObject.put("browseDate", "2025-11-20 12:04:00");
                SimpleDataPackToolsV2 simpleDataPackToolsV2 = new SimpleDataPackToolsV2();
                ChannelRequest channelRequest = simpleDataPackToolsV2.clientPacking(
                        taiKangConfig.get("remotePublicKey"),taiKangConfig.get("localPrivateKey"),
                        jsonObject);
                String send = httpProxyClient.send(channelRequest.toString(), taiKangConfig.get("url"), true);
                System.out.println(send);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
