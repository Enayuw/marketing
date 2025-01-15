package com.br.marketing.mapper;

import com.br.marketing.entity.CallRecordLog;
import com.br.marketing.entity.CallRecordLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CallRecordLogMapperBase {
    int countByExample(CallRecordLogExample example);

    int deleteByExample(CallRecordLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CallRecordLog record);

    int insertSelective(CallRecordLog record);

    List<CallRecordLog> selectByExample(CallRecordLogExample example);

    CallRecordLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CallRecordLog record, @Param("example") CallRecordLogExample example);

    int updateByExample(@Param("record") CallRecordLog record, @Param("example") CallRecordLogExample example);

    int updateByPrimaryKeySelective(CallRecordLog record);

    int updateByPrimaryKey(CallRecordLog record);
}