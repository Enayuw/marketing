package com.br.marketing.mapper.datagroup;

import com.br.marketing.entity.DataGroupTask;
import com.br.marketing.entity.DataGroupTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataGroupTaskMapperBase {
    long countByExample(DataGroupTaskExample example);

    int deleteByExample(DataGroupTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DataGroupTask record);

    int insertSelective(DataGroupTask record);

    List<DataGroupTask> selectByExample(DataGroupTaskExample example);

    DataGroupTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DataGroupTask record, @Param("example") DataGroupTaskExample example);

    int updateByExample(@Param("record") DataGroupTask record, @Param("example") DataGroupTaskExample example);

    int updateByPrimaryKeySelective(DataGroupTask record);

    int updateByPrimaryKey(DataGroupTask record);
}