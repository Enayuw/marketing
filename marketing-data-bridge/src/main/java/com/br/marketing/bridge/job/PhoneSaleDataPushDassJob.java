package com.br.marketing.bridge.job;

import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.ZhongYuanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @Description 电销表数据推送dass人工，承接 SftpToDbByResultDataJob
 * @Author hong.chen
 * @CreateTime 2024/09/10
 */
@Component
@Slf4j
public class PhoneSaleDataPushDassJob extends AbstractSimpleElasticJob {
    @Autowired
    PushDataService pushDataService;

    @Autowired
    LocalFileMapper localFileMapper;

    @Autowired
    ZhongYuanService zhongYuanService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.DX.getValue()).andPushStatusEqualTo("0")
                .andStatusEqualTo("1").andCompleteEqualTo("1");
        List<LocalFile> localFiles = localFileMapper.selectByExample(localFileExample);
        if (CollectionUtils.isEmpty(localFiles)) {
            return;
        }

        HashMap<String, List<String>> dxFileCustomize = marketingCommonConfig.getDxFileCustomize();
        List zhongYuanList = dxFileCustomize.get("zhongYuan");

        for (LocalFile localFile : localFiles) {
            // 更新推送状态为推送中
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setPushStatus("1");
            localFileMapper.updateByPrimaryKeySelective(updateFile);

            pushDataService.pushDassData(localFile.getId());
            if (zhongYuanList.contains(localFile.getApiCode())) {
                zhongYuanService.pushOutBoundData(localFile.getId());
            }

            // 更新推送状态为推送完成
            updateFile.setPushStatus("2");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
        }
    }
}
