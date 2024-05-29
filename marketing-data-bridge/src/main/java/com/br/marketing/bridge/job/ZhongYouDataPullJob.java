package com.br.marketing.bridge.job;

import com.br.marketing.client.zhongyou.ZhongYouDataService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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

    @Resource
    private JobManager jobManager;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        try {
            Result<Long> resultA = actionFront(marketingCommonConfig.getZhongyouApiCode(), 1);
            if (ResultCode.SUCCESS.getValue().equals(resultA.getCode())) {
                String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
                LocalDate beginDate = StringUtils.isBlank(jobParameter) ? LocalDate.now() : LocalDate.parse(jobParameter);
                Result<List<Long>> postResult = zhongYouDataService.saveFileNameList(beginDate);
                if (Objects.equals(postResult.getCode(), ResultCode.SUCCESS.getValue())) {
                    postResult.getData().forEach(fileId -> zhongYouDataService.saveFileData(fileId));
                }
            }
            updateActionFront(resultA);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private  Result<Long> actionFront(String apiCode,Integer actionType){
        Result<TransferActionFront> frontData = jobManager.getFrontData(apiCode, LocalDate.now().toString(), actionType);
        if (!ResultCode.SUCCESS.getValue().equals(frontData.getCode())) {
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        Long jobId;
        TransferActionFront actionFront = frontData.getData();
        if (actionFront == null) {
            jobId = jobManager.saveFrontData(apiCode, LocalDate.now().toString(), actionType);
        } else {
            jobId = actionFront.getId();
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(jobId);
    }
    private void updateActionFront(Result<Long> result) {
        jobManager.updateFrontDataStatus(result.getData(), 2);
    }
}
