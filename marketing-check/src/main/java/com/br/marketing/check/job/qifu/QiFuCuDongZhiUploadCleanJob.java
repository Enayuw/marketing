package com.br.marketing.check.job.qifu;

import com.br.marketing.check.service.Impl.qifu.QiFuDataCleanService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description 促动支上传数据清洗
 * https://c.100credit.cn/pages/viewpage.action?pageId=204898512
 * @Author zhen.Li1
 * @CreateTime 2025/04/08
 */
@Component
@Slf4j
public class QiFuCuDongZhiUploadCleanJob extends AbstractSimpleElasticJob {

    @Autowired
    private QiFuDataCleanService qiFuDataCleanService;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String jobParameter = context.getJobParameter();
        qiFuDataCleanService.cleanUploadData(jobParameter);


    }
}
