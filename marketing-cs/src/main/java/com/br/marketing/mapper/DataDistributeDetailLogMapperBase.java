package com.br.marketing.mapper;

import com.br.marketing.entity.DataDistributeDetailLog;
import com.br.marketing.entity.DataDistributeDetailLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataDistributeDetailLogMapperBase {
    long countByExample(DataDistributeDetailLogExample example);

    int deleteByExample(DataDistributeDetailLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DataDistributeDetailLog record);

    int insertSelective(DataDistributeDetailLog record);

    List<DataDistributeDetailLog> selectByExample(DataDistributeDetailLogExample example);

    DataDistributeDetailLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DataDistributeDetailLog record, @Param("example") DataDistributeDetailLogExample example);

    int updateByExample(@Param("record") DataDistributeDetailLog record, @Param("example") DataDistributeDetailLogExample example);

    int updateByPrimaryKeySelective(DataDistributeDetailLog record);

    int updateByPrimaryKey(DataDistributeDetailLog record);
}