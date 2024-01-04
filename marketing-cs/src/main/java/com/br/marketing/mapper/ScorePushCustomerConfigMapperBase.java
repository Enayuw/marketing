package com.br.marketing.mapper;

import com.br.marketing.entity.ScorePushCustomerConfig;
import com.br.marketing.entity.ScorePushCustomerConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScorePushCustomerConfigMapperBase {
    int countByExample(ScorePushCustomerConfigExample example);

    int deleteByExample(ScorePushCustomerConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ScorePushCustomerConfig record);

    int insertSelective(ScorePushCustomerConfig record);

    List<ScorePushCustomerConfig> selectByExample(ScorePushCustomerConfigExample example);

    ScorePushCustomerConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ScorePushCustomerConfig record, @Param("example") ScorePushCustomerConfigExample example);

    int updateByExample(@Param("record") ScorePushCustomerConfig record, @Param("example") ScorePushCustomerConfigExample example);

    int updateByPrimaryKeySelective(ScorePushCustomerConfig record);

    int updateByPrimaryKey(ScorePushCustomerConfig record);
}