package com.br.marketing.mapper;

import com.br.marketing.entity.EntityOptLog;
import com.br.marketing.entity.EntityOptLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EntityOptLogMapperBase {
    int countByExample(EntityOptLogExample example);

    int deleteByExample(EntityOptLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(EntityOptLog record);

    int insertSelective(EntityOptLog record);

    List<EntityOptLog> selectByExample(EntityOptLogExample example);

    EntityOptLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") EntityOptLog record, @Param("example") EntityOptLogExample example);

    int updateByExample(@Param("record") EntityOptLog record, @Param("example") EntityOptLogExample example);

    int updateByPrimaryKeySelective(EntityOptLog record);

    int updateByPrimaryKey(EntityOptLog record);
}