package com.br.marketing.mapper;

import com.br.marketing.entity.TaskBatchnumberPre;
import com.br.marketing.entity.TaskBatchnumberPreExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskBatchnumberPreMapperBase {
    int countByExample(TaskBatchnumberPreExample example);

    int deleteByExample(TaskBatchnumberPreExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TaskBatchnumberPre record);

    int insertSelective(TaskBatchnumberPre record);

    List<TaskBatchnumberPre> selectByExample(TaskBatchnumberPreExample example);

    TaskBatchnumberPre selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TaskBatchnumberPre record, @Param("example") TaskBatchnumberPreExample example);

    int updateByExample(@Param("record") TaskBatchnumberPre record, @Param("example") TaskBatchnumberPreExample example);

    int updateByPrimaryKeySelective(TaskBatchnumberPre record);

    int updateByPrimaryKey(TaskBatchnumberPre record);
}