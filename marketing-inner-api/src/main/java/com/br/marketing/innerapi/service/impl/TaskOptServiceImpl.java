package com.br.marketing.innerapi.service.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.ZookeeperPath;
import com.br.marketing.entity.*;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.enums.ZkScoreStatusEnum;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.EntityOptService;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TaskOptServiceImpl {

    @Resource
    MarketingTaskMapper marketingTaskMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Autowired
    EntityOptServiceImpl entityOptService;

    TaskStatus taskStatus;

    @Autowired
    CuratorFramework client;

    public Result delTask(Long id) {
        MarketingTask task = marketingTaskMapper.selectByPrimaryKey(id);
        if (task == null) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该跑分不存在");
        }
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria().andBatchNumberEqualTo(task.getBatchNumber());
        List<StraHisFile> files = straHisFileMapper.selectByExample(fileExample);
        Boolean isFinish = Boolean.FALSE;
        if (files.size() > 0) {
            long count = files.stream().filter(t -> !ScoreStatusEnum.FINISH.getValue().equals(t.getStatus())).count();
            isFinish = task.getStatus().equals(1) && count <= 0;
        }
        if (task.getStatus().equals(2) || isFinish) {
            MarketingTask update = new MarketingTask();
            update.setId(id);
            update.setStatus(0);
            marketingTaskMapper.updateByPrimaryKeySelective(update);
            entityOptService.writeOptLog(id, update, task);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("删除成功");
        }
        return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("禁用或者已跑分结束的才能删除");
    }


    public Result pauseTask(Long fileId, Integer isOrPause) {
        StraHisFile straHisFile = straHisFileMapper.selectByPrimaryKey(fileId);
        if (straHisFile == null) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该跑分记录不存在");
        }
        try {
            //region 暂停操作
            if (isOrPause.equals(1)) {

                if (!ScoreStatusEnum.RUNNING.getValue().equals(straHisFile.getStatus())) {
                    return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("跑分调度任务已结束，不支持暂停");
                }

                String filePath = ZookeeperPath.marketStatusPath.concat("/").concat(fileId.toString());

                if (client.checkExists().forPath(filePath) == null) {
                    return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("跑分调度任务启动中，请10分钟后重试");
                }
                String value = new String(client.getData().forPath(filePath));
                if (!ZkScoreStatusEnum.RUNNING.getValue().equals(value)) {
                    return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务不在进行中");
                }
                client.setData().forPath(filePath, ZkScoreStatusEnum.PAUSE.getValue().getBytes(StandardCharsets.UTF_8));
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }
            //endregion
            //region 恢复操作
            if (isOrPause.equals(0)) {
                if (!ScoreStatusEnum.PAUSEED.getValue().equals(straHisFile.getStatus())) {
                    return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务不是已暂停状态");
                }
                String scoreDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(straHisFile.getCreateTime()).substring(0, 10);
                String actionDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                if (!scoreDate.equals(actionDate)) {
                    return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("跨天不允许恢复跑分");
                }
                StraHisFile updateFile = new StraHisFile();
                updateFile.setId(fileId);
                updateFile.setStatus(ScoreStatusEnum.RUNNING.getValue());
                straHisFileMapper.updateByPrimaryKeySelective(updateFile);
                entityOptService.writeOptLog(fileId, updateFile, straHisFile);

                TaskStatusExample statusExample = new TaskStatusExample();
                statusExample.createCriteria().andFileIdEqualTo(fileId.intValue());
                List<TaskStatus> taskStatuses = taskStatusMapper.selectByExample(statusExample);
                TaskStatus taskStatus = taskStatuses.get(0);
                TaskStatus updateStatus = new TaskStatus();
                updateStatus.setId(taskStatus.getId());
                if (new Integer(4).equals(taskStatus.getOnceStatus())) {
                    updateStatus.setOnceStatus(3);
                }
                if (new Integer(4).equals(taskStatus.getAllStatus())) {
                    updateStatus.setAllStatus(3);
                }
                taskStatusMapper.updateByPrimaryKeySelective(updateStatus);
                entityOptService.writeOptLog(Long.valueOf(taskStatus.getId()), updateStatus, taskStatus);
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }
            //endregion
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("操作失败");
    }
}
