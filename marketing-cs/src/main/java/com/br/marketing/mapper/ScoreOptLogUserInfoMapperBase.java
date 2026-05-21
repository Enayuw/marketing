package com.br.marketing.mapper;

import com.br.marketing.entity.ScoreOptLogUserInfo;
import com.br.marketing.entity.ScoreOptLogUserInfoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScoreOptLogUserInfoMapperBase {
    int countByExample(ScoreOptLogUserInfoExample example);

    int deleteByExample(ScoreOptLogUserInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ScoreOptLogUserInfo record);

    int insertSelective(ScoreOptLogUserInfo record);

    List<ScoreOptLogUserInfo> selectByExample(ScoreOptLogUserInfoExample example);

    ScoreOptLogUserInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ScoreOptLogUserInfo record, @Param("example") ScoreOptLogUserInfoExample example);

    int updateByExample(@Param("record") ScoreOptLogUserInfo record, @Param("example") ScoreOptLogUserInfoExample example);

    int updateByPrimaryKeySelective(ScoreOptLogUserInfo record);

    int updateByPrimaryKey(ScoreOptLogUserInfo record);
}
