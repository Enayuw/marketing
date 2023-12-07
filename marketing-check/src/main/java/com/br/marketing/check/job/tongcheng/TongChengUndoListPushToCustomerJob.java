package com.br.marketing.check.job.tongcheng;

import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.Impl.PushRuleServiceImpl;
import com.br.marketing.service.Impl.tongcheng.TongChengUndoListPushToCustomerService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 同程不运营名单推送客户JOB
 * @author chenh
 * @dateTime 2023/12/07 16:13
 */
@Component
@Slf4j
public class TongChengUndoListPushToCustomerJob extends AbstractSimpleElasticJob {

    @Resource
    private LocalFileMapper localFileMapper;

    @Autowired
    private PushRuleServiceImpl pushRuleService;

    @Autowired
    TongChengUndoListPushToCustomerService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        LocalFileExample example = new LocalFileExample();
        //查询待推送文件
        example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.TONGCHENG_UNDO_PUSHTOCUSTOMER.getValue())
                .andStatusEqualTo("2").andPushStatusIsNull();
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(localFiles)) {
            return;
        }
        if (localFiles.size() > 1) {
            log.error("同程不运营名单推送客户JOB异常，推送文件数 size={}", localFiles.size());
            return;
        }
        try {
            Long st1 = System.currentTimeMillis();
            service.process(localFiles.get(0).getId());
            log.warn("同程不运营名单推送客户JOB，耗时：{} ms", System.currentTimeMillis() - st1);
        } catch (Exception e) {
            //推送异常更新状态,更新为失败status=3
            LocalFile localFile = new LocalFile();
            localFile.setPushStatus("3");
            localFile.setId(localFiles.get(0).getId());
            localFileMapper.updateByPrimaryKeySelective(localFile);
            log.error("同程不运营名单推送客户JOB异常", e);
        }

    }
}
