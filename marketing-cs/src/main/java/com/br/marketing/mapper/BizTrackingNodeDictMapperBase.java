package com.br.marketing.mapper;

import com.br.marketing.entity.BizTrackingNodeDict;
import com.br.marketing.entity.BizTrackingNodeDictExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BizTrackingNodeDictMapperBase {
    int countByExample(BizTrackingNodeDictExample example);

    int deleteByExample(BizTrackingNodeDictExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BizTrackingNodeDict record);

    int insertSelective(BizTrackingNodeDict record);

    List<BizTrackingNodeDict> selectByExample(BizTrackingNodeDictExample example);

    BizTrackingNodeDict selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BizTrackingNodeDict record, @Param("example") BizTrackingNodeDictExample example);

    int updateByExample(@Param("record") BizTrackingNodeDict record, @Param("example") BizTrackingNodeDictExample example);

    int updateByPrimaryKeySelective(BizTrackingNodeDict record);

    int updateByPrimaryKey(BizTrackingNodeDict record);
}