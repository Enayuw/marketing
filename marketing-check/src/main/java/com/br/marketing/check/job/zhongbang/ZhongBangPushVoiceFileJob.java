package com.br.marketing.check.job.zhongbang;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * D20240506众邦财富filesdk录音文件上传-3710099
 * https://c.100credit.cn/pages/viewpage.action?pageId=160795358
 *
 * @author Guo Zeqiang
 * @dateTime 2024-05-08 17:36
 */
@Component
@Slf4j
public class ZhongBangPushVoiceFileJob extends AbstractSimpleElasticJob {


    @Override
    public void process(JobExecutionMultipleShardingContext context) {

    }
}
