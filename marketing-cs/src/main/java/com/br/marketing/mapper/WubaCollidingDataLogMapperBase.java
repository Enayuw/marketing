package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataLog;
import com.br.marketing.entity.WubaCollidingDataLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataLogMapperBase {
    int countByExample(WubaCollidingDataLogExample example);

    int deleteByExample(WubaCollidingDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaCollidingDataLog record);

    int insertSelective(WubaCollidingDataLog record);

    List<WubaCollidingDataLog> selectByExample(WubaCollidingDataLogExample example);

    WubaCollidingDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaCollidingDataLog record, @Param("example") WubaCollidingDataLogExample example);

    int updateByExample(@Param("record") WubaCollidingDataLog record, @Param("example") WubaCollidingDataLogExample example);

    int updateByPrimaryKeySelective(WubaCollidingDataLog record);

    int updateByPrimaryKey(WubaCollidingDataLog record);
}