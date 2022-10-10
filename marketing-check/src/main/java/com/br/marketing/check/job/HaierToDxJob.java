package com.br.marketing.check.job;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.IYiXinTransferService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class HaierToDxJob extends AbstractSimpleElasticJob {

    @Autowired
    IYiXinTransferService iYiXinTransferService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        Result result = new Result();
        try {
            if (StringUtils.isNotBlank(jobParameter)) {
                String apiCode = null;
                List<String> params = Splitter.on(",").splitToList(jobParameter);
                apiCode = params.get(0);
                result = iYiXinTransferService.actionHaierToDx(apiCode);
            } else {
                result = iYiXinTransferService.actionHaierToDx(null);
            }
            if(log.isWarnEnabled()){
                log.warn(String.format("推送海尔电销状态：%d,信息：%s",result.getCode(),StringUtils.isNotBlank(result.getMessage())?result.getMessage():""));
            }
        }catch (Exception ex){
                log.error(ex.getMessage(),ex);
        }
    }
}
