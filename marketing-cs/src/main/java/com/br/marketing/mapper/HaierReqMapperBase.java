package com.br.marketing.mapper;

import com.br.marketing.entity.HaierReq;
import com.br.marketing.entity.HaierReqExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HaierReqMapperBase {
    int countByExample(HaierReqExample example);

    int deleteByExample(HaierReqExample example);

    int deleteByPrimaryKey(Long id);

    int insert(HaierReq record);

    int insertSelective(HaierReq record);

    List<HaierReq> selectByExample(HaierReqExample example);

    HaierReq selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") HaierReq record, @Param("example") HaierReqExample example);

    int updateByExample(@Param("record") HaierReq record, @Param("example") HaierReqExample example);

    int updateByPrimaryKeySelective(HaierReq record);

    int updateByPrimaryKey(HaierReq record);
}