package com.br.marketing.mapper;

import com.br.marketing.entity.SmsAccountDetailNormal;
import com.br.marketing.entity.SmsAccountDetailNormalExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsAccountDetailNormalMapperBase {
    int countByExample(SmsAccountDetailNormalExample example);

    int deleteByExample(SmsAccountDetailNormalExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsAccountDetailNormal record);

    int insertSelective(SmsAccountDetailNormal record);

    List<SmsAccountDetailNormal> selectByExample(SmsAccountDetailNormalExample example);

    SmsAccountDetailNormal selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SmsAccountDetailNormal record, @Param("example") SmsAccountDetailNormalExample example);

    int updateByExample(@Param("record") SmsAccountDetailNormal record, @Param("example") SmsAccountDetailNormalExample example);

    int updateByPrimaryKeySelective(SmsAccountDetailNormal record);

    int updateByPrimaryKey(SmsAccountDetailNormal record);
}