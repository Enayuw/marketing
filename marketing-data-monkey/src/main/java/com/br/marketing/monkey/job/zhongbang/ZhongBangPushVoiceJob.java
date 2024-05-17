package com.br.marketing.monkey.job.zhongbang;

import com.br.marketing.service.Impl.zhongbang.IZhongBangPushVoiceService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * 众邦录音文件自动推送
 * 技术文档地址：https://c.100credit.cn/pages/viewpage.action?pageId=160802759
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-16
 */
@Component
@Slf4j
public class ZhongBangPushVoiceJob extends AbstractSimpleElasticJob {

    @Resource
    private IZhongBangPushVoiceService zhongBangPushVoiceServiceImpl;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String uuid = UUID.randomUUID().toString();
        String jobParameter = context.getJobParameter();
        log.warn("ZhongBangPushVoiceJob-start-{}-jobParam:[{}]",uuid,jobParameter);
        zhongBangPushVoiceServiceImpl.pageAndPush();
        log.warn("ZhongBangPushVoiceJob-end-{}", uuid);
    }
}
