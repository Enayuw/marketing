package com.br.marketing.check.job.yizhifu;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName UploadSuccessFileJob
 * @Description 在固定路径下检测指定文件是否存在，若存在且满足条件则上传同名 .success 文件
 * @Author kongbx
 * @Date 2026/3/3 16:01
 */
@Component
@Slf4j
public class UploadSuccessFileJob extends AbstractSimpleElasticJob {

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

    }

}
