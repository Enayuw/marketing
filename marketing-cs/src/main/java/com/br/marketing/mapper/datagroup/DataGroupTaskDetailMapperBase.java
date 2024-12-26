package com.br.marketing.mapper.datagroup;

import com.br.marketing.entity.DataGroupTaskDetail;
import com.br.marketing.entity.DataGroupTaskDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataGroupTaskDetailMapperBase {
    long countByExample(DataGroupTaskDetailExample example);

    int deleteByExample(DataGroupTaskDetailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DataGroupTaskDetail record);

    int insertSelective(DataGroupTaskDetail record);

    List<DataGroupTaskDetail> selectByExample(DataGroupTaskDetailExample example);

    DataGroupTaskDetail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DataGroupTaskDetail record, @Param("example") DataGroupTaskDetailExample example);

    int updateByExample(@Param("record") DataGroupTaskDetail record, @Param("example") DataGroupTaskDetailExample example);

    int updateByPrimaryKeySelective(DataGroupTaskDetail record);

    int updateByPrimaryKey(DataGroupTaskDetail record);
}