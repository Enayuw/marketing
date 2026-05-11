package com.br.marketing.mapper;

import com.br.marketing.entity.MqIdempotentCommon;
import com.br.marketing.entity.MqIdempotentCommonExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MqIdempotentCommonMapperBase {
    int countByExample(MqIdempotentCommonExample example);

    int deleteByExample(MqIdempotentCommonExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MqIdempotentCommon record);

    int insertSelective(MqIdempotentCommon record);

    List<MqIdempotentCommon> selectByExample(MqIdempotentCommonExample example);

    MqIdempotentCommon selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MqIdempotentCommon record, @Param("example") MqIdempotentCommonExample example);

    int updateByExample(@Param("record") MqIdempotentCommon record, @Param("example") MqIdempotentCommonExample example);

    int updateByPrimaryKeySelective(MqIdempotentCommon record);

    int updateByPrimaryKey(MqIdempotentCommon record);
}