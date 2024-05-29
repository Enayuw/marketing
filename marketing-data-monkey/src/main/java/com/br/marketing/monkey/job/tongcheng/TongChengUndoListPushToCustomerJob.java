package com.br.marketing.monkey.job.tongcheng;

import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.Impl.tongcheng.TongChengUndoListPushToCustomerService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
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

    @Resource
    TongChengUndoListPushToCustomerService service;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        marketingCommonConfig.getTongChengUndoApiCodes().forEach((String apiCode) -> {
            LocalFileExample example = new LocalFileExample();
            //查询待推送文件 查询条件b_local_file：status=2 且 push_status=空
            example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.TONGCHENG_UNDO_PUSHTOCUSTOMER.getValue())
                    .andStatusEqualTo("2").andPushStatusIsNull().andApiCodeEqualTo(apiCode);
            List<LocalFile> localFiles = localFileMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(localFiles)) {
                return;
            }

            for (LocalFile localFile : localFiles) {
                try {
                    Long st1 = System.currentTimeMillis();
                    service.process(localFile);
                    log.warn("同程不运营名单推送客户JOB，localFIleId：{}，耗时：{} ms", localFile.getId(), System.currentTimeMillis() - st1);
                } catch (Exception e) {
                    //推送异常更新状态,更新为失败status=3
                    LocalFile localFileFail = new LocalFile();
                    localFileFail.setPushStatus("3");
                    localFileFail.setId(localFile.getId());
                    localFileMapper.updateByPrimaryKeySelective(localFileFail);
                    log.error("同程不运营名单推送客户JOB异常，localFIleId：{}", localFile.getId(), e);
                }
            }
        });
    }
}
