package com.br.marketing.monkey.job.tongcheng;

import com.br.marketing.entity.TongChengAgent;
import com.br.marketing.entity.TongChengAgentExample;
import com.br.marketing.mapper.TongChengAgentMapper;
import com.br.marketing.service.Impl.tongcheng.TongChengOperationPushToCustomerService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 同程集团迁移可营销名单JOB
 * @author guangxiu.li
 * @dateTime 2024/01/25 16:13
 */
@Component
@Slf4j
public class TongChengOperationPushToCustomerJob extends AbstractSimpleElasticJob {
    @Resource
    private TongChengAgentMapper tongChengAgentMapper;

    @Autowired
    TongChengOperationPushToCustomerService service;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        log.warn("同程集团迁移可营销名单JOB调度开始");
        marketingCommonConfig.getTongChengGroupOperationApiCodes().forEach((String apiCode) -> {
            TongChengAgentExample example = new TongChengAgentExample();
            //查询待推数据 查询条件b_tongcheng_agent_mtk_data：status=1 且 push_status=0
            example.createCriteria().andStatusEqualTo(1).andPushStatusEqualTo(0).andApiCodeEqualTo(apiCode);
            List<TongChengAgent> tongChengAgents = tongChengAgentMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(tongChengAgents)) {
                log.warn("同程集团运营名单推送客户量级为空！");
                return;
            }
            try {
                Long st1 = System.currentTimeMillis();
                service.process(apiCode);
                log.warn("同程集团运营名单推送客户JOB，耗时：{} ms",  System.currentTimeMillis() - st1);
            } catch (Exception e) {
                log.error("同程集团运营名单推送客户JOB异常", e);
            }

        });
    }
}
