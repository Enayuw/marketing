package com.br.marketing.mapper;

import com.br.marketing.entity.RoboAIBlackPhoneMark;
import com.br.marketing.entity.RoboAIBlackPhoneMarkExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoboAIBlackPhoneMarkMapperBase {
    long countByExample(RoboAIBlackPhoneMarkExample example);

    int deleteByExample(RoboAIBlackPhoneMarkExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RoboAIBlackPhoneMark record);

    int insertSelective(RoboAIBlackPhoneMark record);

    List<RoboAIBlackPhoneMark> selectByExample(RoboAIBlackPhoneMarkExample example);

    RoboAIBlackPhoneMark selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") RoboAIBlackPhoneMark record, @Param("example") RoboAIBlackPhoneMarkExample example);

    int updateByExample(@Param("record") RoboAIBlackPhoneMark record, @Param("example") RoboAIBlackPhoneMarkExample example);

    int updateByPrimaryKeySelective(RoboAIBlackPhoneMark record);

    int updateByPrimaryKey(RoboAIBlackPhoneMark record);
}