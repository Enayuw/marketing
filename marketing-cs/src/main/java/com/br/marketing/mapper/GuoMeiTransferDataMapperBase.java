package com.br.marketing.mapper;

import com.br.marketing.entity.GuoMeiTransferData;
import com.br.marketing.entity.GuoMeiTransferDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GuoMeiTransferDataMapperBase {
    int countByExample(GuoMeiTransferDataExample example);

    int deleteByExample(GuoMeiTransferDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(GuoMeiTransferData record);

    int insertSelective(GuoMeiTransferData record);

    List<GuoMeiTransferData> selectByExample(GuoMeiTransferDataExample example);

    GuoMeiTransferData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") GuoMeiTransferData record, @Param("example") GuoMeiTransferDataExample example);

    int updateByExample(@Param("record") GuoMeiTransferData record, @Param("example") GuoMeiTransferDataExample example);

    int updateByPrimaryKeySelective(GuoMeiTransferData record);

    int updateByPrimaryKey(GuoMeiTransferData record);
}