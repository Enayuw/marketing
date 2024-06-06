package com.br.marketing.mapper;

import com.br.marketing.entity.PeriodPushLog;
import com.br.marketing.entity.PeriodPushLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PeriodPushLogMapperBase {
    int countByExample(PeriodPushLogExample example);

    int deleteByExample(PeriodPushLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PeriodPushLog record);

    int insertSelective(PeriodPushLog record);

    List<PeriodPushLog> selectByExample(PeriodPushLogExample example);

    PeriodPushLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PeriodPushLog record, @Param("example") PeriodPushLogExample example);

    int updateByExample(@Param("record") PeriodPushLog record, @Param("example") PeriodPushLogExample example);

    int updateByPrimaryKeySelective(PeriodPushLog record);

    int updateByPrimaryKey(PeriodPushLog record);
}