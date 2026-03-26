package com.br.marketing.mapper.wuba;

import com.br.marketing.entity.wuba.WuBaAiConversionData;
import com.br.marketing.entity.wuba.WuBaAiConversionDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WuBaAiConversionDataMapperBase {
    int countByExample(WuBaAiConversionDataExample example);

    int deleteByExample(WuBaAiConversionDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WuBaAiConversionData record);

    int insertSelective(WuBaAiConversionData record);

    List<WuBaAiConversionData> selectByExample(WuBaAiConversionDataExample example);

    WuBaAiConversionData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WuBaAiConversionData record, @Param("example") WuBaAiConversionDataExample example);

    int updateByExample(@Param("record") WuBaAiConversionData record, @Param("example") WuBaAiConversionDataExample example);

    int updateByPrimaryKeySelective(WuBaAiConversionData record);

    int updateByPrimaryKey(WuBaAiConversionData record);
}