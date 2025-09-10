package com.br.marketing.check.job.halo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.service.Impl.halo.IHaloCallbackService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * 【紧急】D20250901哈啰硅基人数据回传-3710212（营销→客户）
 * https://c.100credit.cn/pages/viewpage.action?pageId=220958924
 *
 * @author Hua Qiang
 * @date 2024-10-29 17:53
 */
@Component
@Slf4j
public class HaloCallbackJob extends AbstractSimpleElasticJob {

    @Resource
    private IHaloCallbackService haloCallbackService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();

        JSONObject param = JSON.parseObject(context.getJobParameter());
        String batchNumber = param.getString("batchNumber");
        String whereSql = Optional.ofNullable(param.getString("whereSql")).orElse(" and status = 0 ");
        if(StringUtils.isNotBlank(batchNumber) && StringUtils.isBlank(param.getString("whereSql"))) {
            whereSql = " and status = 2 ";
        }
        haloCallbackService.pushDataCallback(batchNumber, whereSql);
        long end = System.currentTimeMillis();
        log.warn("哈啰硅基人数据回传调度结束, 耗时:{}", end - start);
    }
}
