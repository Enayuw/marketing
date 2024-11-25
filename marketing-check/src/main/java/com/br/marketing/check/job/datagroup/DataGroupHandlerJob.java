package com.br.marketing.check.job.datagroup;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.DataGroupTask;
import com.br.marketing.entity.DataGroupTaskExample;
import com.br.marketing.mapper.datagroup.DataGroupConfigMapper;
import com.br.marketing.mapper.datagroup.DataGroupTaskMapper;
import com.br.marketing.service.datagroup.DataGroupHandlerService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
/**
 * @author zhen.Li1
 * @Classname DataGroupHandlerJob
 * @Description 数据分组流程处理
 * @Date 2024/11/08
 */
public class DataGroupHandlerJob extends AbstractSimpleElasticJob {


    @Autowired
    private DataGroupConfigMapper dataGroupConfigMapper;

    @Autowired
    private DataGroupTaskMapper dataGroupTaskMapper;


    @Resource
    private RedisChgService redisChgService;


    @Autowired
    private DataGroupHandlerService dataGroupHandlerService;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        // 查询分组任务，同一批数据（config_id）放在同一个分片上
        List<DataGroupTask> taskList = dataGroupTaskMapper.selectByShardConfigId(context.getShardingTotalCount(), context.getShardingItems());
        Map<Long, List<DataGroupTask>> taskMap = taskGroup(taskList);
        taskMap.forEach((Long configId, List<DataGroupTask> list) -> {
            for (DataGroupTask dataGroupTask : list) {
                try {
                    dataGroupHandlerService.dataGroupHandler(dataGroupTask);
                    dataGroupTask.setStatus(2);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "分组任务执行过程异常,请关注"), e);
                    dataGroupTask.setStatus(3);
                }
                dataGroupTask.setUpdateTime(new Date());
                dataGroupTaskMapper.updateByPrimaryKeySelective(dataGroupTask);
            }
        });
    }

    private Map<Long, List<DataGroupTask>> taskGroup(List<DataGroupTask> taskList) {

        Map<Long, List<DataGroupTask>> finalTask = new HashMap<>();
        Map<Long, List<DataGroupTask>> gropTaskMap = taskList.stream().collect(Collectors.groupingBy(DataGroupTask::getConfigId));
        gropTaskMap.forEach((Long configId, List<DataGroupTask> list) -> {
            String redisKey = RedisKeyConstant.DATA_GROUP_TASK_LOCK.concat(configId.toString());
            String s = UUID.randomUUID().toString();
            try {
                //处理与页面编辑任务时的并发操作
                redisChgService.lock(redisKey, s);
                DataGroupTaskExample dataGroupTaskExample = new DataGroupTaskExample();
                dataGroupTaskExample.createCriteria().andConfigIdEqualTo(configId).andIsDelEqualTo(1).andStatusEqualTo(0);
                dataGroupTaskExample.setOrderByClause("create_time asc");
                List<DataGroupTask> groupTaskList = dataGroupTaskMapper.selectByExample(dataGroupTaskExample);
                //剔除掉 非 待开始的状态
                List<DataGroupTask> preTask = groupTaskList.stream().filter(task -> task.getStatus() == 0).collect(Collectors.toList());
                preTask.forEach((DataGroupTask groupTask) -> {
                    groupTask.setStatus(1);
                    dataGroupTaskMapper.updateByPrimaryKeySelective(groupTask);
                });
                if (!CollectionUtils.isEmpty(preTask)) {
                    finalTask.put(configId, preTask);
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "分组任务执行过程异常"), e);
            } finally {
                redisChgService.unlock(redisKey, s);
            }
        });

        return finalTask;
    }
}
