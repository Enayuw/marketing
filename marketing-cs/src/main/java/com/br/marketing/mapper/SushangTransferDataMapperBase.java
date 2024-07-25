package com.br.marketing.mapper;

import com.br.marketing.entity.SushangTransferData;
import com.br.marketing.entity.SushangTransferDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SushangTransferDataMapperBase {
    long countByExample(SushangTransferDataExample example);

    int deleteByExample(SushangTransferDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SushangTransferData record);

    int insertSelective(SushangTransferData record);

    List<SushangTransferData> selectByExample(SushangTransferDataExample example);

    SushangTransferData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SushangTransferData record, @Param("example") SushangTransferDataExample example);

    int updateByExample(@Param("record") SushangTransferData record, @Param("example") SushangTransferDataExample example);

    int updateByPrimaryKeySelective(SushangTransferData record);

    int updateByPrimaryKey(SushangTransferData record);
}