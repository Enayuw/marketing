package com.br.marketing.bridge.job;


import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.mapper.MarketingSceneVariableMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName ZhongYuanUploadDataJob
 * @Description 中原消金数据上传Job，查询最新数据并发送MQ消息
 * @Author kongbx
 * @Date 2025/11/17 19:54
 */
@Component
@Slf4j
public class ZhongYuanUpdateSceneVariableJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private MarketingSceneVariableMapper marketingSceneVariableMapper;

    private static final String TITLE = "【中原消金数据上传】";

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        try {
            // 1. 从配置获取中原消金的apiCode
            String apiCode = jobExecutionMultipleShardingContext.getJobParameter();
            if(StringUtils.isEmpty(apiCode)){
                apiCode = getZhongYuanApiCode();
            }

            if (apiCode == null) {
                log.warn("{}Job执行失败：未配置apiCode", TITLE);
                return;
            }

            
        } catch (Exception e) {
            log.error("{}Job执行异常", TITLE, e);
        }
    }

    /**
     * 从配置获取中原消金的apiCode
     */
    private String getZhongYuanApiCode() {
        try {
            Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
            return zhongYuanIdentity != null ? zhongYuanIdentity.get("apiCode") : null;
        } catch (Exception e) {
            log.error("{}获取apiCode异常", TITLE, e);
            return null;
        }
    }

}
