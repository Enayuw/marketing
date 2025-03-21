package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingConfig;
import com.br.marketing.entity.WubaCollidingConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingConfigMapperBase {
    int countByExample(WubaCollidingConfigExample example);

    int deleteByExample(WubaCollidingConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaCollidingConfig record);

    int insertSelective(WubaCollidingConfig record);

    List<WubaCollidingConfig> selectByExample(WubaCollidingConfigExample example);

    WubaCollidingConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaCollidingConfig record, @Param("example") WubaCollidingConfigExample example);

    int updateByExample(@Param("record") WubaCollidingConfig record, @Param("example") WubaCollidingConfigExample example);

    int updateByPrimaryKeySelective(WubaCollidingConfig record);

    int updateByPrimaryKey(WubaCollidingConfig record);
}