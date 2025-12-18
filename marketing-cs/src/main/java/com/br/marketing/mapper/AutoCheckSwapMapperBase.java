package com.br.marketing.mapper;

import com.br.marketing.entity.AutoCheckSwap;
import com.br.marketing.entity.AutoCheckSwapExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AutoCheckSwapMapperBase {
    int countByExample(AutoCheckSwapExample example);

    int deleteByExample(AutoCheckSwapExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AutoCheckSwap record);

    int insertSelective(AutoCheckSwap record);

    List<AutoCheckSwap> selectByExample(AutoCheckSwapExample example);

    AutoCheckSwap selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AutoCheckSwap record, @Param("example") AutoCheckSwapExample example);

    int updateByExample(@Param("record") AutoCheckSwap record, @Param("example") AutoCheckSwapExample example);

    int updateByPrimaryKeySelective(AutoCheckSwap record);

    int updateByPrimaryKey(AutoCheckSwap record);
}