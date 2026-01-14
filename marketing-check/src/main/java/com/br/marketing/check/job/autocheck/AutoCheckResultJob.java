package com.br.marketing.check.job.autocheck;

import com.br.marketing.service.autocheck.AutoCheckService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 自动巡检，检测结果入库
 *
 * @author fuzhen.zhang
 * @date 2025/12/23
 * @description 自动巡检，检测结果入库
 */
@Component
@Slf4j
public class AutoCheckResultJob extends AbstractSimpleElasticJob {

    @Resource
    private AutoCheckService autoCheckService;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        log.info("TITLE:{} 开始执行", "自动巡检，检测结果入库");
        autoCheckService.autoCheck();
        log.info("TITLE:{} 执行完毕", "自动巡检，检测结果入库");
    }
}
