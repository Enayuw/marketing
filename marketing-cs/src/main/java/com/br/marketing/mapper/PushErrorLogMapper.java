package com.br.marketing.mapper;

import com.br.marketing.entity.PushErrorLog;
import com.br.marketing.entity.PushErrorLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PushErrorLogMapper {
    int countByExample(PushErrorLogExample example);

    int deleteByExample(PushErrorLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PushErrorLog record);

    int insertSelective(PushErrorLog record);

    List<PushErrorLog> selectByExample(PushErrorLogExample example);

    PushErrorLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PushErrorLog record, @Param("example") PushErrorLogExample example);

    int updateByExample(@Param("record") PushErrorLog record, @Param("example") PushErrorLogExample example);

    int updateByPrimaryKeySelective(PushErrorLog record);

    int updateByPrimaryKey(PushErrorLog record);
}