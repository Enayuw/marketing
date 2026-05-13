package com.br.marketing.mapper;

import com.br.marketing.entity.IdempotentRecordInfo;
import com.br.marketing.entity.MqIdempotentSpecial;
import com.br.marketing.entity.MqIdempotentSpecialExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MqIdempotentSpecialMapperBase {
    int countByExample(MqIdempotentSpecialExample example);

    int deleteByExample(MqIdempotentSpecialExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MqIdempotentSpecial record);

    int insertSelective(MqIdempotentSpecial record);

    List<MqIdempotentSpecial> selectByExample(MqIdempotentSpecialExample example);

    MqIdempotentSpecial selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MqIdempotentSpecial record, @Param("example") MqIdempotentSpecialExample example);

    int updateByExample(@Param("record") MqIdempotentSpecial record, @Param("example") MqIdempotentSpecialExample example);

    int updateByPrimaryKeySelective(MqIdempotentSpecial record);

    int updateByPrimaryKey(MqIdempotentSpecial record);

}