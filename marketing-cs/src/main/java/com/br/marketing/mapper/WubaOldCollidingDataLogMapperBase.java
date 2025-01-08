package com.br.marketing.mapper;

import com.br.marketing.entity.WubaOldCollidingDataLog;
import com.br.marketing.entity.WubaOldCollidingDataLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaOldCollidingDataLogMapperBase {
    int countByExample(WubaOldCollidingDataLogExample example);

    int deleteByExample(WubaOldCollidingDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaOldCollidingDataLog record);

    int insertSelective(WubaOldCollidingDataLog record);

    List<WubaOldCollidingDataLog> selectByExample(WubaOldCollidingDataLogExample example);

    WubaOldCollidingDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaOldCollidingDataLog record, @Param("example") WubaOldCollidingDataLogExample example);

    int updateByExample(@Param("record") WubaOldCollidingDataLog record, @Param("example") WubaOldCollidingDataLogExample example);

    int updateByPrimaryKeySelective(WubaOldCollidingDataLog record);

    int updateByPrimaryKey(WubaOldCollidingDataLog record);
}