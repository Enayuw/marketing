package com.br.marketing.check.job.zhongyou;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.ZhongYouDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * 描述：： 中邮数据拉取
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYouDataPullJob
 * @author: it-yml
 * @create: 2023-08-02 17:03
 * @Version 1.0
 * --------------------------------------
 **/
@Component
@Slf4j
public class ZhongYouDataPullJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongYouDataService zhongYouDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        try {
            String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
            LocalDate beginDate = StringUtils.isBlank(jobParameter) ? LocalDate.now() : LocalDate.parse(jobParameter);
            Result<List<Long>> postResult = zhongYouDataService.saveFileNameList(beginDate);
            if (postResult.getCode() == ResultCode.SUCCESS.getValue()) {
                postResult.getData().forEach(fileId -> zhongYouDataService.saveFileData(fileId));
            }
        }catch (Exception e){
            log.error("中邮数据拉取定时任务启动异常：{}",e.toString());
        }
    }

}
