package com.br.marketing.mapper;

import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.CaseShuheUserExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CaseShuheUserMapperBase {
    int countByExample(CaseShuheUserExample example);

    int deleteByExample(CaseShuheUserExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CaseShuheUser record);

    int insertSelective(CaseShuheUser record);

    List<CaseShuheUser> selectByExample(CaseShuheUserExample example);

    CaseShuheUser selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CaseShuheUser record, @Param("example") CaseShuheUserExample example);

    int updateByExample(@Param("record") CaseShuheUser record, @Param("example") CaseShuheUserExample example);

    int updateByPrimaryKeySelective(CaseShuheUser record);

    int updateByPrimaryKey(CaseShuheUser record);
}