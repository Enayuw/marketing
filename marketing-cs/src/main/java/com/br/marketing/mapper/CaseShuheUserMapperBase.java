package com.br.marketing.mapper;

import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.CaseShuheUserExample;
import com.br.marketing.entity.CaseShuheUserWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CaseShuheUserMapperBase {
    int countByExample(CaseShuheUserExample example);

    int deleteByExample(CaseShuheUserExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CaseShuheUserWithBLOBs record);

    int insertSelective(CaseShuheUserWithBLOBs record);

    List<CaseShuheUserWithBLOBs> selectByExampleWithBLOBs(CaseShuheUserExample example);

    List<CaseShuheUser> selectByExample(CaseShuheUserExample example);

    CaseShuheUserWithBLOBs selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CaseShuheUserWithBLOBs record, @Param("example") CaseShuheUserExample example);

    int updateByExampleWithBLOBs(@Param("record") CaseShuheUserWithBLOBs record, @Param("example") CaseShuheUserExample example);

    int updateByExample(@Param("record") CaseShuheUser record, @Param("example") CaseShuheUserExample example);

    int updateByPrimaryKeySelective(CaseShuheUserWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(CaseShuheUserWithBLOBs record);

    int updateByPrimaryKey(CaseShuheUser record);
}