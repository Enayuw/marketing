package com.br.marketing.mapper;

import com.br.marketing.entity.QifuActuation;
import com.br.marketing.entity.QifuActuationExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QifuActuationMapperBase {
    int countByExample(QifuActuationExample example);

    int deleteByExample(QifuActuationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(QifuActuation record);

    int insertSelective(QifuActuation record);

    List<QifuActuation> selectByExample(QifuActuationExample example);

    QifuActuation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") QifuActuation record, @Param("example") QifuActuationExample example);

    int updateByExample(@Param("record") QifuActuation record, @Param("example") QifuActuationExample example);

    int updateByPrimaryKeySelective(QifuActuation record);

    int updateByPrimaryKey(QifuActuation record);
}