package com.br.marketing.task.job;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.common.TaskExecCommonField;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.TaskStatus;
import com.br.marketing.entity.TaskStatusExample;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.task.service.Impl.ObservedScoreThreadServiceImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * 暂停跑分任务
 */
@Component
@Slf4j
public class TaskActionJob extends AbstractSimpleElasticJob {

    @Autowired
    ObservedScoreThreadServiceImpl observedScoreThreadService;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    TaskStatusMapper taskStatusMapper;

    @Autowired
    RedisChgService redisChgService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        String[] split = jobParameter.split(",");
        String actionType = split[0];
        if (StringUtils.isBlank(actionType)) {
            return;
        }
        //恢复该节点跑分
        if (actionType.equals("1")) {
            observedScoreThreadService.setInterrupt(1);
            return;
        }

        //暂停该节点跑分
        if (actionType.equals("0")) {
            observedScoreThreadService.stopThread();
            return;
        }

        //恢复该任务跑分
        if (actionType.equals(2)) {
            String fileIdStr = split[1];
            String s = UUID.randomUUID().toString();
            boolean b = addActionLock(fileIdStr, s);
            if(!b){
                return;
            }
            TaskStatusExample statusExample = new TaskStatusExample();
            statusExample.createCriteria().andFileIdEqualTo(Integer.valueOf(fileIdStr));
            List<TaskStatus> taskStatuses = taskStatusMapper.selectByExample(statusExample);
            if (taskStatuses.size() <= 0) {
                return;
            }
            TaskStatus taskStatus = taskStatuses.get(0);
            TaskStatus updateStatus = new TaskStatus();
            updateStatus.setId(taskStatus.getId());

            if (!new Integer(4).equals(taskStatus.getOnceStatus())
                    && !new Integer(4).equals(taskStatus.getAllStatus())) {
                return;
            }
            if (new Integer(4).equals(taskStatus.getOnceStatus())) {
                updateStatus.setAllStatus(3);
            }

            if (new Integer(4).equals(taskStatus.getAllStatus())) {
                updateStatus.setOnceStatus(3);
            }
            taskStatusMapper.updateByPrimaryKeySelective(updateStatus);
            removeActionLock(fileIdStr,s);
        }
    }

    boolean addActionLock(String fileId,String val){
        String key = RedisKeyConstant.taskScoreAction.concat(":").concat(fileId);
        Long res = redisChgService.setnx(key, val, 5);
        if(Long.valueOf(0L).equals(res)){
            return false;
        }
        return true;
    }

    void removeActionLock(String fileId,String val){
        String key = RedisKeyConstant.taskScoreAction.concat(":").concat(fileId);
        String s = redisChgService.get(key);
        if(s.equals(val)){
            redisChgService.del(key);
        }
    }
}
