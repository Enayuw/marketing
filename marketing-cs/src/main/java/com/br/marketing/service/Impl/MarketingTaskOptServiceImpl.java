package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.ZookeeperPath;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CustomerBatchNumDTO;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.TaskStatus;
import com.br.marketing.entity.TaskStatusExample;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.enums.ZkScoreStatusEnum;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.MarketingTaskUserTypeMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.service.MarketingTaskOptService;
import com.br.marketing.vo.CustomerBatchNumVO;
import com.br.marketing.vo.ScoreDetailVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    @Resource
    private MarketingTaskMapper marketingTaskMapper;

    @Resource
    private MarketingTaskUserTypeMapper marketingTaskUserTypeMapper;

    @Autowired(required = false)
    private CuratorFramework client;

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
            //region 暂停操作
            if (isOrPause.equals(1)) {
                return pauseTaskByStraHisFile(1, straHisFile, taskStatus);
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
            //endregion
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

    @Override
    public PageResultReturn<List<ScoreDetailVo>> getBatchInfoFieldList(CustomerBatchNumDTO dto) {
        dto = getCustomerBatchNumDTO(dto);
        PageHelper.startPage(dto.getCurrent(), dto.getSize()).setOrderBy(" scoreBeginTime desc,fileId desc ");
        List<ScoreDetailVo> scoreDetailVos = marketingTaskMapper.queryBatchFieldList(dto);
        scoreDetailVos.forEach((ScoreDetailVo t) -> {
            List<String> batchNumberList = marketingTaskUserTypeMapper.queryUserTypeByBatchNumbertikv_(t.getBatchNumber());
            t.setUserType(String.join(",", batchNumberList));
        });
        return (PageResultReturn<List<ScoreDetailVo>>) PageResultReturn.setPageResult(scoreDetailVos, dto.getCurrent()
                , dto.getSize());
    }

    public CustomerBatchNumDTO getCustomerBatchNumDTO(CustomerBatchNumDTO dto) {
        if (StringUtils.isNotBlank(dto.getProductName())) {
            String productName = dto.getProductName();
            dto.setModuleList(Arrays.asList(productName.split(",")));
        }
        return dto;
    }

    @Override
    public PageResultReturn<List<ScoreDetailVo>> getBatchInfoList(CustomerBatchNumVO batchNumVO) {
        if (batchNumVO.getApiCodeSet() == null || batchNumVO.getApiCodeSet().size() == 0) {
            return (PageResultReturn<List<ScoreDetailVo>>) PageResultReturn.setPageResult(
                    Collections.emptyList(), batchNumVO.getCurrent()
                    , batchNumVO.getSize());
        }
        PageHelper.startPage(batchNumVO.getCurrent(), batchNumVO.getSize()).setOrderBy(" scoreBeginTime desc,fileId desc ");
        List<ScoreDetailVo> scoreDetailVos = marketingTaskMapper.queryBatchList(batchNumVO);
        scoreDetailVos.forEach((ScoreDetailVo t) -> {
            List<String> batchNumberList = marketingTaskUserTypeMapper.queryUserTypeByBatchNumberAndApiCodetikv_(
                    t.getBatchNumber(), t.getApiCode());
            t.setUserType(String.join(",", batchNumberList));
        });
        return (PageResultReturn<List<ScoreDetailVo>>) PageResultReturn.setPageResult(scoreDetailVos, batchNumVO.getCurrent()
                , batchNumVO.getSize());
    }
}
