package com.br.marketing.mapper;

import com.br.marketing.entity.DidiCallBackData;
import com.br.marketing.entity.DidiCallBackDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DidiCallBackDataMapperBase {
    int countByExample(DidiCallBackDataExample example);

    int deleteByExample(DidiCallBackDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DidiCallBackData record);

    int insertSelective(DidiCallBackData record);

    List<DidiCallBackData> selectByExample(DidiCallBackDataExample example);

    DidiCallBackData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DidiCallBackData record, @Param("example") DidiCallBackDataExample example);

    int updateByExample(@Param("record") DidiCallBackData record, @Param("example") DidiCallBackDataExample example);

    int updateByPrimaryKeySelective(DidiCallBackData record);

    int updateByPrimaryKey(DidiCallBackData record);
}