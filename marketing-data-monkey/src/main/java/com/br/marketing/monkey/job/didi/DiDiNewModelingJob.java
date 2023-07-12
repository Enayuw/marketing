package com.br.marketing.monkey.job.didi;

import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.monkeydata.entity.commonobj.MarketingSyncCondition;
import com.br.marketing.monkeydata.handle.didi.DiDiNewModelingDataHandle;
import com.br.marketing.service.Impl.YiXinTransferServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
/**
 * @author lizhen
 * @Description 滴滴联合建模新接口Job
 * @Date 2023/07/11 20:02
 */
@Component
@Slf4j
public class DiDiNewModelingJob extends AbstractSimpleElasticJob {

    final static String EXECUTE_TIME = " 10:30:00";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;

    @Resource
    private YiXinTransferServiceImpl yiXinTransferService;
    @Resource
    private DiDiNewModelingDataHandle diDiNewModelingDataHandle;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String apiCode = "3710083";
        String jobParameter = context.getJobParameter();
        if (StringUtils.isNotBlank(jobParameter)) {
            apiCode = jobParameter;
        }
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(marketingCommonConfig.getDidiModelingNewExecTime())) {
            execute = " " + marketingCommonConfig.getDidiModelingNewExecTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        String recordDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (!now.before(executeTime)) {
            //查询推送记录
            List<TransferActionFront> actionFrontList = getActionFront(apiCode, 3);
            if (actionFrontList.size() > 0) {
                log.warn("滴滴联合建模新接口今日已经推送");
                return;
            }
            Long frontId = yiXinTransferService.saveFrontData(apiCode, recordDate, 3);

            MarketingSyncCondition marketingSyncCondition = new MarketingSyncCondition();
            marketingSyncCondition.setApiCode(apiCode);
            diDiNewModelingDataHandle.action(marketingSyncCondition);
            yiXinTransferService.updateFrontDataStatus(frontId, 2);
            log.warn("滴滴联合建模新接口任务完成");
        }
    }


    private List<TransferActionFront> getActionFront(String apiCode, int actionType) {
        TransferActionFrontExample example = new TransferActionFrontExample();
        TransferActionFrontExample.Criteria criteria = example.createCriteria();
        criteria.andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .andActionTypeEqualTo(actionType)
                .andIsDelEqualTo(1);
        return transferActionFrontMapper.selectByExample(example);
    }
}
