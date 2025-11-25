package com.br.marketing.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TaikangTransferDataLogMapperBase {
    int countByExample(TaikangTransferDataLogExample example);

    int deleteByExample(TaikangTransferDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TaikangTransferDataLog record);

    int insertSelective(TaikangTransferDataLog record);

    List<TaikangTransferDataLog> selectByExampleWithBLOBs(TaikangTransferDataLogExample example);

    List<TaikangTransferDataLog> selectByExample(TaikangTransferDataLogExample example);

    TaikangTransferDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TaikangTransferDataLog record, @Param("example") TaikangTransferDataLogExample example);

    int updateByExampleWithBLOBs(@Param("record") TaikangTransferDataLog record, @Param("example") TaikangTransferDataLogExample example);

    int updateByExample(@Param("record") TaikangTransferDataLog record, @Param("example") TaikangTransferDataLogExample example);

    int updateByPrimaryKeySelective(TaikangTransferDataLog record);

    int updateByPrimaryKeyWithBLOBs(TaikangTransferDataLog record);

    int updateByPrimaryKey(TaikangTransferDataLog record);
}