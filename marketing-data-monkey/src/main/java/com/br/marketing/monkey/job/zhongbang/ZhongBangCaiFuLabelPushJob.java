package com.br.marketing.monkey.job.zhongbang;


import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.Impl.PushRuleServiceImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 众邦财富定制标签推送Job
 *
 * @author zhen.Li1
 * @dateTime 2023/11/27 16:13
 */
@Component
@Slf4j
public class ZhongBangCaiFuLabelPushJob extends AbstractSimpleElasticJob {

    @Resource
    private LocalFileMapper localFileMapper;

    @Autowired
    private PushRuleServiceImpl pushRuleService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        LocalFileExample example = new LocalFileExample();
        //查询待推送文件
        example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.ZHONGBANGLABEL.getValue())
                .andStatusEqualTo("2").andPushStatusIsNull();
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(localFiles)) {
            return;
        }
        if (localFiles.size() > 1) {
            log.error("众邦财富定制标签推送文件数异常，推送文件数 size={}", localFiles.size());
            return;
        }
        try {
            pushRuleService.cunsumerZhongBangLabelData(localFiles.get(0).getId());
        } catch (Exception e) {
            //推送异常更新状态,更新为失败status=3
            LocalFile localFile = new LocalFile();
            localFile.setPushStatus("3");
            localFile.setId(localFiles.get(0).getId());
            localFileMapper.updateByPrimaryKeySelective(localFile);
            log.error("众邦财富定制标签推送异常", e);
        }

    }
}
