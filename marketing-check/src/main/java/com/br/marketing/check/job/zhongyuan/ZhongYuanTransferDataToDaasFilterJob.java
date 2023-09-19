package com.br.marketing.check.job.zhongyuan;

import com.br.marketing.service.ZhongYuanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;


/**
 * 中原转化数据推Daas转化
 * @ClassName ZhongYuanTransferDataToDaasFilterJob
 * @author: zhen.Li
 * @create: 2023-08-30 19:34
 * @Version 1.0
 * --------------------------------------
 **/
@Component
@Slf4j
public class ZhongYuanTransferDataToDaasFilterJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongYuanService zhongYuanService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String parameter = context.getJobParameter();
        Set<String> zhongYuanJobApiCodes = marketingCommonConfig.getZhongYuanJobApiCodes();
        zhongYuanJobApiCodes.forEach(apiCode -> {
        if("1".equals(parameter)){
            zhongYuanService.zhongYuanPushDaasTransferFirst(apiCode);
        }else{
            zhongYuanService.zhongYuanPushDaasTransfer(apiCode);
        }});
        log.warn("中原推送Daas转化接口任务结束");
    }
}
