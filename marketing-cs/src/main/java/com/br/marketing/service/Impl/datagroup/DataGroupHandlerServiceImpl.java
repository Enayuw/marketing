package com.br.marketing.service.Impl.datagroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.datagroup.DataGroupConfgDTO;
import com.br.marketing.entity.*;
import com.br.marketing.service.datagroup.DataGroupHandlerService;
import com.br.marketing.mapper.datagroup.DataGroupConfigMapper;
import com.br.marketing.mapper.datagroup.DataGroupTaskMapper;
import com.br.marketing.vo.datagroup.DataGropRuleVO;
import com.br.marketing.vo.datagroup.DataGroupConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 数据分组service实现
 * @Author zhen.Li1
 * @CreateTime 2024/11/07
 */
@Service
@Slf4j
public class DataGroupHandlerServiceImpl implements DataGroupHandlerService {

    @Autowired
    private DataGroupConfigMapper dataGroupConfigMapper;

    @Autowired
    private DataGroupTaskMapper dataGroupTaskMapper;

    @Resource
    private RedisChgService redisChgService;


    /**
     * 获取分组配置
     *
     * @param ids 上传记录id集合
     * @return List<DataGroupConfigVO>
     */
    @Override
    public List<DataGroupConfigVO> configList(String ids, String apiCode) {
        List<String> idList = Arrays.asList(ids.split(","));
        Collections.sort(idList);
        List<DataGroupConfigVO> dataGroupConfigList;
        if (idList.size() > 1) {

            dataGroupConfigList = dataGroupConfigMapper.selectGroupConfigList(String.join(",", idList), "1", apiCode);
        } else {
            dataGroupConfigList = dataGroupConfigMapper.selectGroupConfigList(ids, "2", apiCode);

        }
        return dataGroupConfigList;
    }

    @Override
    public Result updateConfig(DataGroupConfgDTO dto) {
        List<String> idList = Arrays.asList(dto.getIds().split(","));
        Collections.sort(idList);
        List<DataGropRuleVO> gropRuleVOList = JSON.parseObject(dto.getGroupRules(), new TypeReference<List<DataGropRuleVO>>() {
        }.getType());
        Map<String, List<DataGropRuleVO>> gropRuleMap = gropRuleVOList.stream().collect(Collectors.groupingBy(DataGropRuleVO::getGroupField));
        String redisKey = RedisKeyConstant.DATA_GROUP_TASK_LOCK.concat(dto.getId().toString());
        String s = UUID.randomUUID().toString();
        try {
            //处理与定时任务执行时的并发操作
            redisChgService.lock(redisKey, s);
            DataGroupTaskExample dataGroupTaskExample = new DataGroupTaskExample();
            dataGroupTaskExample.createCriteria().andApiCodeEqualTo(dto.getApiCode()).andConfigIdEqualTo(dto.getId()).andOperTypeEqualTo(0);
            List<DataGroupTask> groupTaskList = dataGroupTaskMapper.selectByExample(dataGroupTaskExample);
            List<DataGroupTask> runingTask = groupTaskList.stream().filter(task -> task.getStatus() != 0).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(runingTask)) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("分组任务已开始执行，无法进行编辑");
            }
            groupTaskList.forEach((DataGroupTask groupTask) -> {
                groupTask.setGroupRule(JSON.toJSONString(gropRuleMap.get(groupTask.getGroupFiled()).get(0)));
                dataGroupTaskMapper.updateByPrimaryKeySelective(groupTask);
            });
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "分组任务编辑异常"), e);
        } finally {
            redisChgService.unlock(redisKey, s);
        }
        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(dto.getId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addOrDeleteConfig(DataGroupConfgDTO dto) {
        List<String> idList = Arrays.asList(dto.getIds().split(","));
        Collections.sort(idList);
        String reportId = String.join(",", idList);
        String ruleJson = dto.getGroupRules();
        String operType = dto.getOperType();
        List<DataGropRuleVO> gropRuleVOList = JSON.parseObject(ruleJson, new TypeReference<List<DataGropRuleVO>>() {
        }.getType());
        Map<String, List<DataGropRuleVO>> gropRuleMap = gropRuleVOList.stream().collect(Collectors.groupingBy(DataGropRuleVO::getGroupField));
        DataGroupConfigExample dataGroupConfigExample = new DataGroupConfigExample();
        DataGroupConfigExample.Criteria criteria = dataGroupConfigExample.createCriteria();
        criteria.andApiCodeEqualTo(dto.getApiCode()).andUploadReportIdEqualTo(reportId);
        List<DataGroupConfig> groupConfigList = dataGroupConfigMapper.selectByExample(dataGroupConfigExample);
        Long configId;
        if (CollectionUtils.isEmpty(groupConfigList)) {
            DataGroupConfig dataGroupConfig = new DataGroupConfig();
            dataGroupConfig.setApiCode(dto.getApiCode());
            dataGroupConfig.setGroupRules(JSON.toJSONString(gropRuleVOList));
            dataGroupConfig.setUploadReportId(reportId);
            dataGroupConfig.setCreateTime(new Date());
            dataGroupConfig.setUpdateTime(new Date());
            dataGroupConfigMapper.insertSelective(dataGroupConfig);
            configId = dataGroupConfig.getId();
        } else {
            DataGroupConfig update = groupConfigList.get(0);
            configId = update.getId();
            List<DataGropRuleVO> updateGroupRules = JSON.parseObject(update.getGroupRules(), new TypeReference<List<DataGropRuleVO>>() {
            }.getType());
            if (operType.equals("0")) {
                updateGroupRules.addAll(gropRuleVOList);
            } else {
                updateGroupRules.removeIf(rule -> gropRuleMap.keySet().contains(rule.getGroupField()));
                dataGroupConfigMapper.updateByPrimaryKeySelective(update);
                //TODO 更新跑分配置 && 判断正在进行中的跑分不生成删除任务
            }
            update.setGroupRules(JSON.toJSONString(gropRuleVOList));
            dataGroupConfigMapper.updateByPrimaryKeySelective(update);
        }
        // 插入 任务表
        gropRuleMap.forEach((String field, List<DataGropRuleVO> groupRules) -> {
            DataGroupTask dataGroupTask = new DataGroupTask();
            dataGroupTask.setApiCode(dto.getApiCode());
            dataGroupTask.setConfigId(configId);
            dataGroupTask.setGroupFiled(field);
            dataGroupTask.setGroupRule(JSON.toJSONString(groupRules.get(0)));
            dataGroupTask.setOperType(Integer.valueOf(operType));
            dataGroupTask.setStatus(0);
            dataGroupTask.setCreateTime(new Date());
            dataGroupTask.setUpdateTime(new Date());
            dataGroupTaskMapper.insertSelective(dataGroupTask);

        });
        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(configId);
    }

    @Override
    public void dataGroupHandler(DataGroupTask dataGroupTask) {

        DataGroupConfig config = dataGroupConfigMapper.selectByPrimaryKey(dataGroupTask.getConfigId());
        if (dataGroupTask.getOperType().equals(0)) {
            addFieldHandler(config.getUploadReportId(), dataGroupTask);
        } else {
            delFieldHandler(config.getUploadReportId(), dataGroupTask);
        }
    }

    private void delFieldHandler(String uploadReportId, DataGroupTask dataGroupTask) {
        DataGropRuleVO rule = JSON.parseObject(dataGroupTask.getGroupRule(), new TypeReference<DataGropRuleVO>() {
        }.getType());
        //TODO 查询上传数据，组装批量更新sql，执行sql
    }

    private void addFieldHandler(String uploadReportId, DataGroupTask dataGroupTask) {

        DataGropRuleVO rule = JSON.parseObject(dataGroupTask.getGroupRule(), new TypeReference<DataGropRuleVO>() {
        }.getType());
        //TODO 分组


    }
}
