package com.br.marketing.service.thirdpartner;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.ThirdPartnerDataPassBackTask;
import com.br.marketing.entity.ThirdPartnerDataPassBackTaskExample;
import com.br.marketing.enums.ThirdPartnerDataPassBackTaskPushStatusEnum;
import com.br.marketing.mapper.ThirdPartnerDataPassBackTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 三方数据处理实现类
 * @Author hong.chen
 * @CreateTime 2024/11/28
 */
@Service
@Slf4j
public class ThirdPartnerDataServiceImpl implements ThirdPartnerDataService{
    
    @Resource
    ThirdPartnerDataPassBackTaskMapper thirdPartnerDataPassBackTaskMapper;
    
    
    @Override
    public void validChangeDataBack() {
        //1.查询【b_third_partner_data_pass_back_task】
        List<ThirdPartnerDataPassBackTask> taskList = queryTask();
        if(taskList.isEmpty()){
            return;
        }
        //遍历处理task
        taskList.forEach(task -> {
            processTask(task);
        });

    }

    /**
     * @description 处理任务主流程
     * @param task
     * @return void
     * @author hedongshuo
     * @date 2024/11/28 21:39
     **/
    private void processTask(ThirdPartnerDataPassBackTask task) {

    }

    /**
     * @description 查询所有未执行的任务
     * @return java.util.List<com.br.marketing.entity.ThirdPartnerDataPassBackTask>
     * @author hedongshuo
     * @date 2024/11/28 21:00
     **/
    private List<ThirdPartnerDataPassBackTask> queryTask() {
        ThirdPartnerDataPassBackTaskExample example = new ThirdPartnerDataPassBackTaskExample();
        example.createCriteria()
                .andPushStatusNotEqualTo(ThirdPartnerDataPassBackTaskPushStatusEnum.FINISHED.getPushStatus())
                .andIsDeletedEqualTo(0);
        example.setOrderByClause("create_time asc");
        List<ThirdPartnerDataPassBackTask> thirdPartnerDataPassBackTasks = thirdPartnerDataPassBackTaskMapper.selectByExample(example);
        if (thirdPartnerDataPassBackTasks.isEmpty()) {
            return thirdPartnerDataPassBackTasks;
        }
        //有处理中的task，说明流程有异常，需要告警
        long executingCount = thirdPartnerDataPassBackTasks.stream()
                .filter(task -> task.getPushStatus() == ThirdPartnerDataPassBackTaskPushStatusEnum.EXECUTING.getPushStatus()).count();
        if (executingCount > 0) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.EXCEPTION_URGENT.getCode(), "三方数据有效期变更回传任务，有处理中的任务，请关注！"));
        }
        return thirdPartnerDataPassBackTasks.stream()
                .filter(task -> task.getPushStatus() == ThirdPartnerDataPassBackTaskPushStatusEnum.WAITED_EXECUTE.getPushStatus())
                .collect(Collectors.toList());
    }
}
