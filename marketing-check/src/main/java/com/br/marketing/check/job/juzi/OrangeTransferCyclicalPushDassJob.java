package com.br.marketing.check.job.juzi;

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
import java.util.*;

/**
 * 桔子周期性自动化转Daas-3710037（营销→Daas）
 *
 * @author Guo Zeqiang
 * @dateTime 2022/7/19 10:18
 */
@Component
@Slf4j
public class OrangeTransferCyclicalPushDassJob extends AbstractSimpleElasticJob {

    @Resource
    private OrangePushDassService orangePushDassService;

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        List<String> list = new ArrayList<>(Collections.singletonList("3710037"));
        String parameter = shardingContext.getJobParameter();
        boolean nextBool = true;
        if (StringUtils.isNotEmpty(parameter)) {
            StringTokenizer string = new StringTokenizer(parameter, ",");
            while (string.hasMoreTokens()) {
                String apiCode = string.nextToken();
                if (list.contains(apiCode)) {
                    if (nextBool) {
                        nextBool = false;
                    }
                    continue;
                }
                list.add(apiCode);
            }
        }
        for (String apiCode : list) {
            List<TransferActionFront> actionFrontList = getActionFront(apiCode, 2, 3);
            int size = actionFrontList.size();
            // 1. 前置任务是否完成
            if (size < 1 && nextBool) {
                continue;
            }
            actionFrontList = getActionFront(apiCode, null, 4);
            size = actionFrontList.size();
            // 2. 周期性任务是否已存在
            if (size > 0) {
                continue;
            }
            TransferActionFront taf = new TransferActionFront();
            taf.setApiCode(apiCode);
            taf.setCreateTime(new Date());
            taf.setUpdateTime(taf.getCreateTime());
            taf.setActionType(4);
            taf.setStatus(1);
            taf.setIsDel(1);
            taf.setActionData(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            transferActionFrontMapper.insertSelective(taf);
            orangePushDassService.transferCyclicalPushDass(apiCode);
            TransferActionFront tafUpdate = new TransferActionFront();
            tafUpdate.setStatus(2);
            tafUpdate.setId(taf.getId());
            transferActionFrontMapper.updateByPrimaryKeySelective(tafUpdate);
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
