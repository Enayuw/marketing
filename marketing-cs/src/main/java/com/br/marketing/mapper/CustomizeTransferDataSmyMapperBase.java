package com.br.marketing.mapper;

import com.br.marketing.entity.CustomizeTransferDataSmy;
import com.br.marketing.entity.CustomizeTransferDataSmyExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomizeTransferDataSmyMapperBase {
    int countByExample(CustomizeTransferDataSmyExample example);

    int deleteByExample(CustomizeTransferDataSmyExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CustomizeTransferDataSmy record);

    int insertSelective(CustomizeTransferDataSmy record);

    List<CustomizeTransferDataSmy> selectByExample(CustomizeTransferDataSmyExample example);

    CustomizeTransferDataSmy selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CustomizeTransferDataSmy record, @Param("example") CustomizeTransferDataSmyExample example);

    int updateByExample(@Param("record") CustomizeTransferDataSmy record, @Param("example") CustomizeTransferDataSmyExample example);

    int updateByPrimaryKeySelective(CustomizeTransferDataSmy record);

    int updateByPrimaryKey(CustomizeTransferDataSmy record);
}