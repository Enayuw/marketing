package com.br.marketing.mapper;

import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.entity.WubaSubmitConversionDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaSubmitConversionDataMapperBase {
    int countByExample(WubaSubmitConversionDataExample example);

    int deleteByExample(WubaSubmitConversionDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaSubmitConversionData record);

    int insertSelective(WubaSubmitConversionData record);

    List<WubaSubmitConversionData> selectByExample(WubaSubmitConversionDataExample example);

    WubaSubmitConversionData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaSubmitConversionData record, @Param("example") WubaSubmitConversionDataExample example);

    int updateByExample(@Param("record") WubaSubmitConversionData record, @Param("example") WubaSubmitConversionDataExample example);

    int updateByPrimaryKeySelective(WubaSubmitConversionData record);

    int updateByPrimaryKey(WubaSubmitConversionData record);
}