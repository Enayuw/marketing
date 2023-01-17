package com.br.marketing.check.job;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.br.marketing.common.utils.MQConstants.ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGSMSCOLLIDINGRECEIVE;

/**
 * @author guangchao.zhang
 * @Classname CallingToSendJob
 * @Description 回调第三方接口发送不打信息
 * @Date 2022/2/16 10:02 AM
 */
@Component
@Slf4j
public class XieChengSmsDataCollidingToSendJob extends AbstractSimpleElasticJob {

    private final static String XIECHENGSMSCOLLIDING = "xiechengsmscolliding";

    @Autowired
    RabbitMqProducter producter;
    @Resource
    private LocalFileMapper localFileMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria()
                .andFileTypeEqualTo(XIECHENGSMSCOLLIDING)
                .andStatusEqualTo("1");
        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);


        localFileList.stream().forEach((localFile) -> {
            JSONObject msg = new JSONObject();
            msg.put("localId", localFile.getId());
            msg.put("type", 2);
            producter.send(ROUTING_KEY_UNIVERSAL_SFTPTODB_XIECHENGSMSCOLLIDINGRECEIVE, msg.toJSONString());
        });
    }

}
