package com.br.marketing.mapper;

import com.br.marketing.entity.TaskTime;
import com.br.marketing.entity.TaskTimeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskTimeMapperBase {
    int countByExample(TaskTimeExample example);

    int deleteByExample(TaskTimeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TaskTime record);

    int insertSelective(TaskTime record);

    List<TaskTime> selectByExample(TaskTimeExample example);

    TaskTime selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TaskTime record, @Param("example") TaskTimeExample example);

    int updateByExample(@Param("record") TaskTime record, @Param("example") TaskTimeExample example);

    int updateByPrimaryKeySelective(TaskTime record);

    int updateByPrimaryKey(TaskTime record);
}