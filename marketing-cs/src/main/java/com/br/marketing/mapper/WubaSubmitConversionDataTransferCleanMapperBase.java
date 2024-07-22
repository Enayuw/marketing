package com.br.marketing.mapper;

import com.br.marketing.entity.WubaSubmitConversionDataTransferClean;
import com.br.marketing.entity.WubaSubmitConversionDataTransferCleanExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaSubmitConversionDataTransferCleanMapperBase {
    int countByExample(WubaSubmitConversionDataTransferCleanExample example);

    int deleteByExample(WubaSubmitConversionDataTransferCleanExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaSubmitConversionDataTransferClean record);

    int insertSelective(WubaSubmitConversionDataTransferClean record);

    List<WubaSubmitConversionDataTransferClean> selectByExample(WubaSubmitConversionDataTransferCleanExample example);

    WubaSubmitConversionDataTransferClean selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaSubmitConversionDataTransferClean record,
                                 @Param("example") WubaSubmitConversionDataTransferCleanExample example);

    int updateByExample(@Param("record") WubaSubmitConversionDataTransferClean record,
                        @Param("example") WubaSubmitConversionDataTransferCleanExample example);

    int updateByPrimaryKeySelective(WubaSubmitConversionDataTransferClean record);

    int updateByPrimaryKey(WubaSubmitConversionDataTransferClean record);
}