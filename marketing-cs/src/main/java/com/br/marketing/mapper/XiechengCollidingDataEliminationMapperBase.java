package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.XiechengCollidingDataElimination;
import com.br.marketing.entity.XiechengCollidingDataEliminationExample;

public interface XiechengCollidingDataEliminationMapperBase {
    int countByExample(XiechengCollidingDataEliminationExample example);

    int deleteByExample(XiechengCollidingDataEliminationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XiechengCollidingDataElimination record);

    int insertSelective(XiechengCollidingDataElimination record);

    List<XiechengCollidingDataElimination> selectByExample(XiechengCollidingDataEliminationExample example);

    XiechengCollidingDataElimination selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XiechengCollidingDataElimination record,
                                 @Param("example") XiechengCollidingDataEliminationExample example);

    int updateByExample(@Param("record") XiechengCollidingDataElimination record,
                        @Param("example") XiechengCollidingDataEliminationExample example);

    int updateByPrimaryKeySelective(XiechengCollidingDataElimination record);

    int updateByPrimaryKey(XiechengCollidingDataElimination record);
}