package com.br.marketing.mapper.llm;

import com.br.marketing.entity.llm.CybotstarAgentConfig;
import com.br.marketing.entity.llm.CybotstarAgentConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CybotstarAgentConfigMapperBase {
    int countByExample(CybotstarAgentConfigExample example);

    int deleteByExample(CybotstarAgentConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CybotstarAgentConfig record);

    int insertSelective(CybotstarAgentConfig record);

    List<CybotstarAgentConfig> selectByExample(CybotstarAgentConfigExample example);

    CybotstarAgentConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CybotstarAgentConfig record, @Param("example") CybotstarAgentConfigExample example);

    int updateByExample(@Param("record") CybotstarAgentConfig record, @Param("example") CybotstarAgentConfigExample example);

    int updateByPrimaryKeySelective(CybotstarAgentConfig record);

    int updateByPrimaryKey(CybotstarAgentConfig record);
}