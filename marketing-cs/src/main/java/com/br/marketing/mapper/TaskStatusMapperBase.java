package com.br.marketing.mapper;

import com.br.marketing.entity.TaskStatus;
import com.br.marketing.entity.TaskStatusExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskStatusMapperBase {
    int countByExample(TaskStatusExample example);

    int deleteByExample(TaskStatusExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TaskStatus record);

    int insertSelective(TaskStatus record);

    List<TaskStatus> selectByExample(TaskStatusExample example);

    TaskStatus selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TaskStatus record, @Param("example") TaskStatusExample example);

    int updateByExample(@Param("record") TaskStatus record, @Param("example") TaskStatusExample example);

    int updateByPrimaryKeySelective(TaskStatus record);

    int updateByPrimaryKey(TaskStatus record);
}