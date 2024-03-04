package com.br.marketing.check.job;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.XieChengSmsCollidingDataExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.XieChengSmsCollidingDataMapper;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author guangchao.zhang
 * @Classname CallingToSendJob
 * @Description 携程异常重试job
 * @Date 2024/2/26 10:02 AM
 */
@Component
@Slf4j
public class RetryXieChengSmsDataCollidingToSendJob extends AbstractSimpleElasticJob {
    private final static String XIECHENGSMSCOLLIDING = "xiechengsmscolliding";
    @Resource
    private PushDataService pushDataService;
    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private XieChengSmsCollidingDataMapper xieChengSmsCollidingDataMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        XieChengSmsCollidingDataExample x = new  XieChengSmsCollidingDataExample();
        x.createCriteria().andStatusEqualTo(1).andRetryCountIn(Arrays.asList(1,2,3,4));
        int countedByExample = xieChengSmsCollidingDataMapper.countByExample(x);
        if(countedByExample>=marketingCommonConfig.getXieChengSmsCollidingRetryWarnCount()){
            // 发送钉钉告警
            DingDingMarkdownMessage.Markdown markdown = new DingDingMarkdownMessage.Markdown();
            markdown.setTitle("携程撞库异常量级过大通知");
            markdown.setText("3710058携程重试堆积量级已超" + countedByExample+"条。"
            );
            DingDingMarkdownMessage dingDingMarkdownMessage = new DingDingMarkdownMessage();
            dingDingMarkdownMessage.setMarkdown(markdown);
            dingDingRobotHookService.sendMessageGroup(marketingCommonConfig.getXieChengGroupAccessToken() ,
                    marketingCommonConfig.getXieChengGroupSecret(), dingDingMarkdownMessage, true);
            return;
        }

        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria()
                .andFileTypeEqualTo(XIECHENGSMSCOLLIDING)
                .andStatusEqualTo("2");
        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);
        localFileList.forEach((lf) ->
                pushDataService.retryPushXieChengSmsCollidingToDbData(lf.getId())
        );
    }
}
