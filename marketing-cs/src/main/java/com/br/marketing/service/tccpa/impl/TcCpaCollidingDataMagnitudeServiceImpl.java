package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.tccpa.TcCpaDeleteRuleExecuteInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCollectStatusEnum;
import com.br.marketing.enums.TcCpaCollidingSourceTypeEnum;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.mapper.TcyrCpaCollectTaskMapper;
import com.br.marketing.mapper.TcyrCpaCollidingTaskMapper;
import com.br.marketing.mapper.TcyrCpaDeleteRuleMapper;
import com.br.marketing.service.tccpa.TcCpaCollidingDataMagnitudeService;
import com.br.marketing.service.tccpa.TcCpaCommonService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class TcCpaCollidingDataMagnitudeServiceImpl implements TcCpaCollidingDataMagnitudeService {

    private final static String TITLE = "【同程易融CPA-colliding data check任务】";

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private TcyrCpaCollectTaskMapper tcyrCpaCollectTaskMapper;

    @Resource
    private TcyrCpaCollidingTaskMapper tcyrCpaCollidingTaskMapper;

    @Resource
    private TcCpaCommonService tcCpaCommonService;

    @Resource
    private TcyrCpaDeleteRuleMapper tcyrCpaDeleteRuleMapper;

    @Override
    public void process() {
        calMagnitude();

    }

    // 计算量级
    private void calMagnitude() {
        // 判断当日统计任务是否完成
        TcyrCpaCollectTaskExample example = new TcyrCpaCollectTaskExample();
        example.createCriteria().andStatusLessThan(TcCpaCollectStatusEnum.DEAL_SUCCESS.getValue())
                .andSourceTypeIn(Lists.newArrayList(TcCpaCollidingSourceTypeEnum.SUCCESS.getValue(),
                        TcCpaCollidingSourceTypeEnum.FAIL.getValue()));
        if (tcyrCpaCollectTaskMapper.countByExample(example) > 0) {
            return;
        }

        // 更新剔除规则对应量级
        TcyrCpaDeleteRuleExample deleteRuleExample = new TcyrCpaDeleteRuleExample();
        List<TcyrCpaDeleteRule> deleteRules = tcyrCpaDeleteRuleMapper.selectByExample(deleteRuleExample);
        deleteRules.forEach(deleteRule -> {
            try {
                List<TcCpaDeleteRuleExecuteInfoDTO> executeInfos = objectMapper.readValue(deleteRule.getExecuteInfo(),
                        new TypeReference<List<TcCpaDeleteRuleExecuteInfoDTO>>() {
                        });
                deleteRule.setDeleteNum(tcCpaCommonService.calculateVolume(executeInfos));
                tcyrCpaDeleteRuleMapper.updateByPrimaryKey(deleteRule);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "剔除规则更新剔除量级失败，规则id：" + deleteRule.getId(), TITLE), e);
            }
        });

        // 更新撞库任务
        TcyrCpaCollidingTaskExample collidingExample = new TcyrCpaCollidingTaskExample();
        collidingExample.createCriteria().andCollidingDateEqualTo(new Date())
                .andStatusEqualTo(TcCpaCollidingTaskStatusEnum.STATUS_WAIT_STA.getValue())
                .andEnabledEqualTo(Constants.ENABLED_ACT).andIsDelEqualTo(Constants.DATA_VALID);
        List<TcyrCpaCollidingTask> collidingTasks = tcyrCpaCollidingTaskMapper.selectByExample(collidingExample);

        for (TcyrCpaCollidingTask collidingTask : collidingTasks) {
            try {
                tcCpaCommonService.updateVolumeByTask(collidingTask);
                collidingTask.setStatus(TcCpaCollidingTaskStatusEnum.STATUS_STA_COMPLETED.getValue());
                tcyrCpaCollidingTaskMapper.updateByPrimaryKey(collidingTask);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "撞库任务更新量级失败，taskId：" + collidingTask.getId(), TITLE), e);
            }
        }
    }

}
