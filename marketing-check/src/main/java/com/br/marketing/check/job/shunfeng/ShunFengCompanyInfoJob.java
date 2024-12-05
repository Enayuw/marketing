package com.br.marketing.check.job.shunfeng;

import com.br.common.log.AlertLog;
import com.br.marketing.check.service.Impl.shunfeng.ShunFengService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 顺丰获取公司信息job
 * @Author zhen.Li1
 * @CreateTime 2024/11/18
 */
@Component
@Slf4j
public class ShunFengCompanyInfoJob extends AbstractSimpleElasticJob {


    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private ShunFengService shunFengService;


    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        //查询待推送文件
        LocalFileExample example = new LocalFileExample();
        example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.SHUNFENG_COMPANY.getValue())
                .andStatusEqualTo("2").andPushStatusIsNull();
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(localFiles)) {
            return;
        }
        try {
            shunFengService.getCompanyDetail(localFiles.get(0).getId());
        } catch (Exception e) {
            //推送异常更新状态,更新为失败status=3
            LocalFile localFile = new LocalFile();
            localFile.setPushStatus("3");
            localFile.setId(localFiles.get(0).getId());
            localFileMapper.updateByPrimaryKeySelective(localFile);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(), "【顺丰获取公司信息】处理异常！"), e);
        }


    }
}
