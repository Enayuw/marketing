package com.br.marketing.bridge.job;

import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.PushDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description 电销表数据推送dassIbu，承接 SftpToDbByDxIbuDataJob
 * @Author hong.chen
 * @CreateTime 2024/09/10
 */
@Component
@Slf4j
public class PhoneSaleDataPushDassIbuJob extends AbstractSimpleElasticJob {
    @Autowired
    PushDataService pushDataService;

    @Autowired
    LocalFileMapper localFileMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.DXIBU.getValue()).andPushStatusEqualTo("0")
                .andStatusEqualTo("1").andCompleteIn(Lists.newArrayList("1", "3"));
        List<LocalFile> localFiles = localFileMapper.selectByExample(localFileExample);
        if (CollectionUtils.isEmpty(localFiles)) {
            return;
        }

        for (LocalFile localFile : localFiles) {
            // 更新推送状态为推送中
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setPushStatus("1");
            localFileMapper.updateByPrimaryKeySelective(updateFile);

            pushDataService.pushDassTransferIbu(localFile.getId());

            // 更新推送状态为推送完成
            updateFile.setPushStatus("2");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
        }
    }
}
