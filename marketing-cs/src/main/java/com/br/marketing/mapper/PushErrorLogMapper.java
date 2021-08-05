package com.br.marketing.mapper;

import com.br.marketing.entity.PushErrorLog;
import com.br.marketing.entity.PushErrorLogExample;
import com.br.marketing.entity.PushErrorLogWithBLOBs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushErrorLogMapper {
    int countByExample(PushErrorLogExample example);

    int deleteByExample(PushErrorLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PushErrorLogWithBLOBs record);

    int insertSelective(PushErrorLogWithBLOBs record);

    List<PushErrorLogWithBLOBs> selectByExampleWithBLOBs(PushErrorLogExample example);

    List<PushErrorLog> selectByExample(PushErrorLogExample example);

    PushErrorLogWithBLOBs selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PushErrorLogWithBLOBs record, @Param("example") PushErrorLogExample example);

    int updateByExampleWithBLOBs(@Param("record") PushErrorLogWithBLOBs record, @Param("example") PushErrorLogExample example);

    int updateByExample(@Param("record") PushErrorLog record, @Param("example") PushErrorLogExample example);

    int updateByPrimaryKeySelective(PushErrorLogWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(PushErrorLogWithBLOBs record);

    int updateByPrimaryKey(PushErrorLog record);
}