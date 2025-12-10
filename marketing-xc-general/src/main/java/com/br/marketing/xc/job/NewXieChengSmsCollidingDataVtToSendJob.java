package com.br.marketing.xc.job;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.PushDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 携程CPS黑名单撞库
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @author: it-yml
 * @create: 2023-07-11 19:31
 * @Version 1.0
 * --------------------------------------
 **/
@Component
@Slf4j
public class NewXieChengSmsCollidingDataVtToSendJob extends AbstractSimpleElasticJob {
    private static final String XIECHENGSMSCOLLIDINGVT = "xiechengsmscollidingvt";

    /**
     * 推送实现
     */
    @Resource
    private PushDataService pushDataService;

    /**
     * jobParameter 为需要推送数据的最小id 减 1
     * @param jobExecutionMultipleShardingContext
     */
    /**
     * 文件
     */
    @Resource
    private LocalFileMapper localFileMapper;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        final LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria()
                .andFileTypeEqualTo(XIECHENGSMSCOLLIDINGVT)
                .andStatusEqualTo("2").andPushStatusIsNull();
        localFileExample.setOrderByClause("id desc");
        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);
        localFileList.forEach((LocalFile lf) -> pushDataService.pushXieChengSmsCollidingToDbDataVt(lf.getId()));
    }


}
