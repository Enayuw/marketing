package com.br.marketing.mapper.ningbo;

import com.br.marketing.entity.ningbo.NingBoDataTask;
import com.br.marketing.entity.ningbo.NingBoDataTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NingBoDataTaskMapperBase {
    int countByExample(NingBoDataTaskExample example);

    int deleteByExample(NingBoDataTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NingBoDataTask record);

    int insertSelective(NingBoDataTask record);

    List<NingBoDataTask> selectByExample(NingBoDataTaskExample example);

    NingBoDataTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NingBoDataTask record, @Param("example") NingBoDataTaskExample example);

    int updateByExample(@Param("record") NingBoDataTask record, @Param("example") NingBoDataTaskExample example);

    int updateByPrimaryKeySelective(NingBoDataTask record);

    int updateByPrimaryKey(NingBoDataTask record);
}