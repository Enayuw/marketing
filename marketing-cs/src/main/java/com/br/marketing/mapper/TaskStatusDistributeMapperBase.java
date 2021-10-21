package com.br.marketing.mapper;

import com.br.marketing.entity.TaskStatusDistribute;
import com.br.marketing.entity.TaskStatusDistributeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskStatusDistributeMapperBase {
    int countByExample(TaskStatusDistributeExample example);

    int deleteByExample(TaskStatusDistributeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TaskStatusDistribute record);

    int insertSelective(TaskStatusDistribute record);

    List<TaskStatusDistribute> selectByExample(TaskStatusDistributeExample example);

    TaskStatusDistribute selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TaskStatusDistribute record, @Param("example") TaskStatusDistributeExample example);

    int updateByExample(@Param("record") TaskStatusDistribute record, @Param("example") TaskStatusDistributeExample example);

    int updateByPrimaryKeySelective(TaskStatusDistribute record);

    int updateByPrimaryKey(TaskStatusDistribute record);
}