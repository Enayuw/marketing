package com.br.marketing.monkey.job.qifu;

import com.br.marketing.entity.*;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.Impl.YiXinTransferServiceImpl;
import com.br.marketing.service.Impl.qifu.QiFuQrySleepUserRealMessageService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;


/**
 * @ClassName QiFuQrySleepUserRealMessageJob
 * @Description 促动支用户信息3710139（营销→客户）
 * @Author kongbx
 * @Date 2024/6/25 11:25
 */
@Component
@Slf4j
public class QiFuQrySleepUserRealMessageJob extends AbstractSimpleElasticJob {

    @Autowired
    QiFuQrySleepUserRealMessageService service;

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;

    @Resource
    private YiXinTransferServiceImpl yiXinTransferService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        int actionTypeTransfer = JobManager.ActionTypeEnum.QIFU_TRIGGER_BRANCH_USER.getActionType();

        long start = System.currentTimeMillis();
        String parameter = shardingContext.getJobParameter();
        String apiCode = "3710139";
        LocalDate now = LocalDate.now();
        if (StringUtils.isNotBlank(parameter)) {
            String[] parames = parameter.split(":");
            if (parames.length == 1) {
                apiCode = parames[0];
            }
        }
        // 查询今日是否执行过任务
        List<TransferActionFront> actionFrontList = getActionFront(apiCode, now.toString(),actionTypeTransfer);
        int status = 2;
        if (!actionFrontList.isEmpty()) {
            TransferActionFront actionFront = actionFrontList.get(0);
            if (status == actionFront.getStatus()) {
                log.warn("api_code:{}【奇富促动支用户信息】该任务今日已经推送", apiCode);
            } else {
                log.warn("api_code:{}【奇富促动支用户信息】该任务今日已经已有任务在运行"
                        , apiCode);
                return;
            }
        } else {
            // 记录作业执行日志
            Long frontId = yiXinTransferService.saveFrontData(apiCode, now.toString(), actionTypeTransfer);
            // 逻辑处理
            service.process(apiCode);
            // 处理完成，将作业状态置为执行结束
            yiXinTransferService.updateFrontDataStatus(frontId, 2);
        }

        long end = System.currentTimeMillis();
        log.warn("【奇富促动支用户信息】调度结束apiCodes:{}, 耗时:{}", apiCode, end - start);
    }

    private List<TransferActionFront> getActionFront(String apiCode, String bizDate,int actionType) {
        TransferActionFrontExample example = new TransferActionFrontExample();
        TransferActionFrontExample.Criteria criteria = example.createCriteria();
        criteria.andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(bizDate)
                .andActionTypeEqualTo(actionType)
                .andIsDelEqualTo(1);
        return transferActionFrontMapper.selectByExample(example);
    }


}
