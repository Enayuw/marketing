package com.br.marketing.mapper;

import com.br.marketing.entity.TaikangTransferDataLog;
import com.br.marketing.entity.TaikangTransferDataLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TaikangTransferDataLogMapperBase {
    int countByExample(TaikangTransferDataLogExample example);

    int deleteByExample(TaikangTransferDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TaikangTransferDataLog record);

    int insertSelective(TaikangTransferDataLog record);

    List<TaikangTransferDataLog> selectByExample(TaikangTransferDataLogExample example);

    TaikangTransferDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TaikangTransferDataLog record, @Param("example") TaikangTransferDataLogExample example);

    int updateByExample(@Param("record") TaikangTransferDataLog record, @Param("example") TaikangTransferDataLogExample example);

    int updateByPrimaryKeySelective(TaikangTransferDataLog record);

    int updateByPrimaryKey(TaikangTransferDataLog record);
}