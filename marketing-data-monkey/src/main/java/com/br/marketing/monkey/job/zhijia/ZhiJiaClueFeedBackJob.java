package com.br.marketing.monkey.job.zhijia;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.Impl.zhijia.ZhiJiaClueFeedBackService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;


/**
 * @ClassName ZhiJiaClueFeedBackJob
 * @Description 之家线索回传
 * @Author kongbx
 * @Date 2024/7/8 20:02
 */
@Component
@Slf4j
public class ZhiJiaClueFeedBackJob extends AbstractSimpleElasticJob {

    @Autowired
    ZhiJiaClueFeedBackService service;

    @Resource
    private LocalFileMapper localFileMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        //查询待推送文件
        LocalFileExample example = new LocalFileExample();
        example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.ZHIJIACLUE.getValue())
                .andStatusEqualTo("2").andPushStatusIsNull();
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(localFiles)) {
            return;
        }
        try {
            service.process(localFiles.get(0).getId());
        } catch (Exception e) {
            //推送异常更新状态,更新为失败status=3
            LocalFile localFile = new LocalFile();
            localFile.setPushStatus("3");
            localFile.setId(localFiles.get(0).getId());
            localFileMapper.updateByPrimaryKeySelective(localFile);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHIJIA_SERVICEERROR.getCode(), "【之家创建线索】推送异常！"), e);
        }

    }
}
