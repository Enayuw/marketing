package com.br.marketing.check.job.xiecheng;

import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.Impl.PushDataServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 携程短信退订推送Job
 *
 * @author zhen.Li1
 * @dateTime 2023/12/07 16:13
 */
@Component
@Slf4j
public class XieChengSftpToSmsQuitJob extends AbstractSimpleElasticJob {

    @Resource
    private LocalFileMapper localFileMapper;

    @Autowired
    private PushDataServiceImpl pushDataServiceImpl;

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        LocalFileExample example = new LocalFileExample();
        //查询待推送文件
        example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.XIECHENGSMSQUIT.getValue())
                .andStatusEqualTo("2").andPushStatusIsNull().andApiCodeIn(marketingCommonConfig.getXieChengSmsQuitApiCodes());
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(localFiles)) {
            return;
        }

        try {
            localFiles.forEach((LocalFile localFile) -> {
                pushDataServiceImpl.pushSmsQuitData(localFile);
            });
        } catch (Exception e) {
            //推送异常更新状态,更新为失败status=3
            LocalFile localFile = new LocalFile();
            localFile.setPushStatus("3");
            localFile.setId(localFiles.get(0).getId());
            localFileMapper.updateByPrimaryKeySelective(localFile);
            log.error("携程短信退订推送异常", e);
        }

    }
}
