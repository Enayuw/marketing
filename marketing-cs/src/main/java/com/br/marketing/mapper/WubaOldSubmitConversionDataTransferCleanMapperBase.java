package com.br.marketing.mapper;

import com.br.marketing.entity.WubaOldSubmitConversionDataTransferClean;
import com.br.marketing.entity.WubaOldSubmitConversionDataTransferCleanExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaOldSubmitConversionDataTransferCleanMapperBase {

    int countByExample(WubaOldSubmitConversionDataTransferCleanExample example);

    int deleteByExample(WubaOldSubmitConversionDataTransferCleanExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaOldSubmitConversionDataTransferClean record);

    int insertSelective(WubaOldSubmitConversionDataTransferClean record);

    List<WubaOldSubmitConversionDataTransferClean> selectByExample(WubaOldSubmitConversionDataTransferCleanExample example);

    WubaOldSubmitConversionDataTransferClean selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaOldSubmitConversionDataTransferClean record,
                                 @Param("example") WubaOldSubmitConversionDataTransferCleanExample example);

    int updateByExample(@Param("record") WubaOldSubmitConversionDataTransferClean record,
                        @Param("example") WubaOldSubmitConversionDataTransferCleanExample example);

    int updateByPrimaryKeySelective(WubaOldSubmitConversionDataTransferClean record);

    int updateByPrimaryKey(WubaOldSubmitConversionDataTransferClean record);
}