package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.dto.DataExportTaskDTO;
import com.br.marketing.entity.DataExportTask;
import com.br.marketing.entity.DataExportTaskExample;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.DataSourceEnum;
import com.br.marketing.mapper.DataExportTaskMapper;
import com.br.marketing.service.IDataExportTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 数据导出任务服务实现
 *
 * @author guangxiu.li
 * @date 2025/06/10
 * @description
 */
@Service
@Slf4j
public class IDataExportTaskServiceImpl implements IDataExportTaskService {

    @Resource
    private DataExportTaskMapper dataExportTaskMapper;

    @Override
    public Long createTask(DataExportTaskDTO dto, MarketingUserDetail user) {
        log.info("开始创建数据导出任务，参数：{}, 用户：{}", dto, user.getUserName());
        
        try {
            // 1. 参数验证
            if (dto == null) {
                log.error("DTO参数为空");
                return null;
            }

            // 2. 检查任务名称是否重复
            if (isTaskNameExists(dto.getTaskName())) {
                log.error("任务名称已存在：{}", dto.getTaskName());
                return null;
            }

            // 3. 验证数据源是否有效
            DataSourceEnum dataSourceEnum = getDataSourceEnum(dto.getDataSource());
            if (dataSourceEnum == null) {
                log.error("无效的数据源code：{}", dto.getDataSource());
                return null;
            }

            // 4. DTO转换为Entity
            DataExportTask task = convertToEntity(dto, user, dataSourceEnum);

            // 5. 保存到数据库
            int result = dataExportTaskMapper.insertSelective(task);
            
            if (result > 0 && task.getId() != null) {
                log.info("数据导出任务创建成功，任务ID：{}, 任务名称：{}", task.getId(), task.getTaskName());
                return task.getId();
            } else {
                log.error("数据导出任务保存失败");
                return null;
            }

        } catch (Exception e) {
            log.error("创建数据导出任务异常，参数：{}", dto, e);
            return null;
        }
    }


    /**
     * 检查任务名称是否已存在
     */
    private boolean isTaskNameExists(String taskName) {
        try {
            DataExportTaskExample example = new DataExportTaskExample();
            example.createCriteria()
                   .andTaskNameEqualTo(taskName)
                   .andStatusEqualTo(1);
            
            List<DataExportTask> existingTasks = dataExportTaskMapper.selectByExample(example);
            return !CollectionUtils.isEmpty(existingTasks);
        } catch (Exception e) {
            log.error("检查任务名称是否存在时异常", e);
            return true;
        }
    }

    /**
     * 根据前端传入的code获取数据源枚举
     * @param dataSourceCode 前端传入的数据源code（数字字符串）
     * @return 数据源枚举，如果不存在返回null
     */
    private DataSourceEnum getDataSourceEnum(String dataSourceCode) {
        try {
            // 前端传的是code（数字），需要转换为Integer
            Integer code = Integer.valueOf(dataSourceCode);
            return DataSourceEnum.getByCode(code);
        } catch (NumberFormatException e) {
            log.error("数据源code格式错误：{}", dataSourceCode);
            return null;
        }
    }

    /**
     * DTO转换为Entity
     */
    private DataExportTask convertToEntity(DataExportTaskDTO dto, MarketingUserDetail user, DataSourceEnum dataSourceEnum) {
        DataExportTask task = new DataExportTask();
        
        // 基本信息
        task.setTaskName(dto.getTaskName());
        task.setDataSource(dataSourceEnum.getSourceCode());
        task.setExportHeaders(dto.getExportHeaders());
        task.setEstimatedRows(dto.getEstimatedRows());
        
        // 文件名模板：如果为空则生成默认模板
        String fileNameTemplate = StringUtils.isNotBlank(dto.getFileNameTemplate())
            ? dto.getFileNameTemplate() 
            : dto.getTaskName() + ".txt";
        task.setFileNameTemplate(fileNameTemplate);
        
        // JSON字段序列化
        if (!CollectionUtils.isEmpty(dto.getFieldMapping())) {
            task.setFieldMapping(JSON.toJSONString(dto.getFieldMapping()));
        }
        
        if (!CollectionUtils.isEmpty(dto.getQueryCondition())) {
            task.setQueryCondition(JSON.toJSONString(dto.getQueryCondition()));
        }
        
        // 默认状态：启用
        task.setStatus(1);
        
        // 创建人信息
        task.setCreateBy(user.getUserName());
        task.setUpdateBy(user.getUserName());
        
        // 时间信息
        Date now = new Date();
        task.setCreateTime(now);
        task.setUpdateTime(now);
        
        return task;
    }


}
