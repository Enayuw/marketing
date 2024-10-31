package com.br.marketing.mapper.guomei;

import com.br.marketing.entity.guomei.GuoMeiCallbackData;
import com.br.marketing.entity.guomei.GuoMeiCallbackDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GuoMeiCallbackDataMapperBase {
    int countByExample(GuoMeiCallbackDataExample example);

    int deleteByExample(GuoMeiCallbackDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(GuoMeiCallbackData record);

    int insertSelective(GuoMeiCallbackData record);

    List<GuoMeiCallbackData> selectByExample(GuoMeiCallbackDataExample example);

    GuoMeiCallbackData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") GuoMeiCallbackData record, @Param("example") GuoMeiCallbackDataExample example);

    int updateByExample(@Param("record") GuoMeiCallbackData record, @Param("example") GuoMeiCallbackDataExample example);

    int updateByPrimaryKeySelective(GuoMeiCallbackData record);

    int updateByPrimaryKey(GuoMeiCallbackData record);
}