package com.br.marketing.monkey.job.qifu;

import com.br.marketing.monkey.service.qifu.QiFuQueryCallService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 奇富360定制查询外呼信息Job
 * 1、查询Redis开关（user_type有卷比例>=75% 或者 当前时间>12:10）
 * 2、根据开关状态查询数据：
 *    - 开关打开：select_status in (0, 3)
 *    - 开关关闭：select_status in (0, 3, 4)
 * 3、查询非实时数据（event_type为空）
 * 4、调用360查询接口
 * 5、返回信息存在extend里
 */
@Component
@Slf4j
public class QiFuQueryCallRealTimeJob extends AbstractSimpleElasticJob {

    @Resource
    private QiFuQueryCallService qiFuQueryCallService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("奇富360定制查询外呼信息开始");
        long start = System.currentTimeMillis();
        qiFuQueryCallService.queryCallMessage();
        log.warn("奇富360定制查询外呼信息耗时{} ms", System.currentTimeMillis() - start);
    }
}

