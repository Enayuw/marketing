package com.br.marketing.service.Impl;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.ZookeeperPath;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.TaskStatus;
import com.br.marketing.entity.TaskStatusExample;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.enums.ZkScoreStatusEnum;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.service.MarketingTaskOptService;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description TaskOptServiceImpl
 * @Author hong.chen
 * @CreateTime 2024/06/14
 */
@Service
@Slf4j
public class MarketingTaskOptServiceImpl implements MarketingTaskOptService {
    @Autowired
    StraHisFileMapper straHisFileMapper;

    @Autowired
    EntityOptServiceImpl entityOptService;

    @Resource
    TaskStatusMapper taskStatusMapper;

    @Autowired(required = false)
    private CuratorFramework client;

    @Autowired
    private ScoreCrossDayRestoreService scoreCrossDayRestoreService;

    @Override
    public Result pauseTask(Long fileId, Integer isOrPause) {
        StraHisFile straHisFile = straHisFileMapper.selectByPrimaryKey(fileId);
        if (straHisFile == null) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该跑分记录不存在");
        }

        TaskStatusExample statusExample = new TaskStatusExample();
        statusExample.createCriteria().andFileIdEqualTo(fileId.intValue());
        List<TaskStatus> taskStatuses = taskStatusMapper.selectByExample(statusExample);
        if (CollectionUtils.isEmpty(taskStatuses)) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("跑分执行状态表中未找到该跑分任务");
        }
        TaskStatus taskStatus = taskStatuses.get(0);

        try {
            // region 暂停操作
            if (isOrPause.equals(1)) {
                return pauseTaskByStraHisFile(1, straHisFile, taskStatus);
            }
            // endregion
            // region 恢复操作
            if (isOrPause.equals(0)) {
                if (!ScoreStatusEnum.PAUSEED.getValue().equals(straHisFile.getStatus())) {
                    return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务不是已暂停状态");
                }
                Result crossDay = scoreCrossDayRestoreService.prepareForResume(straHisFile);
                if (!ResultCode.SUCCESS.getValue().equals(crossDay.getCode())) {
                    return crossDay;
                }
                StraHisFile updateFile = new StraHisFile();
                updateFile.setId(fileId);
                updateFile.setStatus(ScoreStatusEnum.RUNNING.getValue());
                straHisFileMapper.updateByPrimaryKeySelective(updateFile);
                entityOptService.writeOptLog(fileId, updateFile, straHisFile);

                TaskStatus updateStatus = new TaskStatus();
                updateStatus.setId(taskStatus.getId());

                if (Objects.equals(taskStatus.getOnceStatus(), 4)) {
                    updateStatus.setOnceStatus(3);
                }
                if (Objects.equals(taskStatus.getAllStatus(), 4)) {
                    updateStatus.setAllStatus(3);
                }
                taskStatusMapper.updateByPrimaryKeySelective(updateStatus);
                entityOptService.writeOptLog(Long.valueOf(taskStatus.getId()), updateStatus, taskStatus);
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }
            // endregion
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("操作失败");
    }

    @Override
    public Result pauseTaskByStraHisFile(Integer pauseType, StraHisFile straHisFile, TaskStatus taskStatus) {
        if (!ScoreStatusEnum.RUNNING.getValue().equals(straHisFile.getStatus())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("跑分调度任务已结束，不支持暂停");
        }

        String filePath = ZookeeperPath.marketStatusPath.concat("/").concat(straHisFile.getId().toString());

        try {
            if (client.checkExists().forPath(filePath) == null) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("跑分调度任务启动中，请10分钟后重试");
            }

            String value = new String(client.getData().forPath(filePath));
            if (!ZkScoreStatusEnum.RUNNING.getValue().equals(value)) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务不在进行中");
            }

            // 更新状态表暂停类型
            TaskStatus updateStatus = new TaskStatus();
            updateStatus.setId(taskStatus.getId());
            updateStatus.setPauseType(pauseType);
            taskStatusMapper.updateByPrimaryKeySelective(updateStatus);

            // zk节点置为暂停中
            client.setData().forPath(filePath, ZkScoreStatusEnum.PAUSE.getValue().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
