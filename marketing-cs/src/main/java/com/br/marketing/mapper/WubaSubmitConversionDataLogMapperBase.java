package com.br.marketing.mapper;

import com.br.marketing.entity.WubaSubmitConversionDataLog;
import com.br.marketing.entity.WubaSubmitConversionDataLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaSubmitConversionDataLogMapperBase {

    int countByExample(WubaSubmitConversionDataLogExample example);

    int deleteByExample(WubaSubmitConversionDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaSubmitConversionDataLog record);

    int insertSelective(WubaSubmitConversionDataLog record);

    List<WubaSubmitConversionDataLog> selectByExample(WubaSubmitConversionDataLogExample example);

    WubaSubmitConversionDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaSubmitConversionDataLog record, @Param("example") WubaSubmitConversionDataLogExample example);

    int updateByExample(@Param("record") WubaSubmitConversionDataLog record, @Param("example") WubaSubmitConversionDataLogExample example);

    int updateByPrimaryKeySelective(WubaSubmitConversionDataLog record);

    int updateByPrimaryKey(WubaSubmitConversionDataLog record);
}