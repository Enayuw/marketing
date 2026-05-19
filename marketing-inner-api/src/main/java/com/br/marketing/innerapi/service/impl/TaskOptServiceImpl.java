package com.br.marketing.innerapi.service.impl;

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
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.Impl.ScoreCrossDayRestoreService;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TaskOptServiceImpl {

    @Autowired
    StraHisFileMapper straHisFileMapper;

    @Autowired
    EntityOptServiceImpl entityOptService;

    @Resource
    TaskStatusMapper taskStatusMapper;

    @Autowired
    private CuratorFramework client;

    @Autowired
    private ScoreCrossDayRestoreService scoreCrossDayRestoreService;

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
                Result crossDay = scoreCrossDayRestoreService.prepareForResume(straHisFile);
                if (!ResultCode.SUCCESS.getValue().equals(crossDay.getCode())) {
                    return crossDay;
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
