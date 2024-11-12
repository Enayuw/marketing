package com.br.marketing.mapper;

import com.br.marketing.entity.SynInfoQueryAction;
import com.br.marketing.entity.SynInfoQueryActionExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SynInfoQueryActionMapperBase {
    int countByExample(SynInfoQueryActionExample example);

    int deleteByExample(SynInfoQueryActionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SynInfoQueryAction record);

    int insertSelective(SynInfoQueryAction record);

    List<SynInfoQueryAction> selectByExample(SynInfoQueryActionExample example);

    SynInfoQueryAction selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SynInfoQueryAction record, @Param("example") SynInfoQueryActionExample example);

    int updateByExample(@Param("record") SynInfoQueryAction record, @Param("example") SynInfoQueryActionExample example);

    int updateByPrimaryKeySelective(SynInfoQueryAction record);

    int updateByPrimaryKey(SynInfoQueryAction record);
}