package com.br.marketing.check.job.orange;

import com.br.marketing.check.service.OrangePushDassService;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 桔子周期性自动化转Daas-3710037（营销→Daas）
 *
 * @author Guo Zeqiang
 * @dateTime 2022/7/19 10:18
 */
@Component
@Slf4j
public class OrangeTransferCyclicalPushDaasJob extends AbstractSimpleElasticJob {

    @Resource
    private OrangePushDassService orangePushDassService;

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;

    private final static List<String> API_CODE_LIST = new ArrayList<>();

    static {
        API_CODE_LIST.add("3710037");
    }

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        String parameter = shardingContext.getJobParameter();
        if (StringUtils.isNotEmpty(parameter)) {
            StringTokenizer stringTokenizer = new StringTokenizer(parameter, ",");
            while (stringTokenizer.hasMoreTokens()) {
                API_CODE_LIST.add(stringTokenizer.nextToken());
            }
        }
        for (String apiCode : API_CODE_LIST) {
            List<TransferActionFront> actionFrontList = getActionFront(apiCode, 2, 3);
            int size = actionFrontList.size();
            /*
             * 1. 前置任务是否完成
             *
             */
            if (size < 1) {
                continue;
            }
            actionFrontList = getActionFront(apiCode, null, 4);
            size = actionFrontList.size();
            if (size > 0) {
                continue;
            }
            orangePushDassService.transferCyclicalPushDaas(apiCode);
        }
        long end = System.currentTimeMillis();
        log.warn("【桔子周期性自动化转Daas】调度结束，耗时:{}", end - start);

    }

    private List<TransferActionFront> getActionFront(String apiCode, Integer status, int actionType) {
        TransferActionFrontExample example = new TransferActionFrontExample();
        TransferActionFrontExample.Criteria criteria = example.createCriteria();
        criteria.andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .andActionTypeEqualTo(actionType)
                .andIsDelEqualTo(1);
        if (status != null) {
            criteria.andStatusEqualTo(status);
        }
        return transferActionFrontMapper.selectByExample(example);
    }
}
