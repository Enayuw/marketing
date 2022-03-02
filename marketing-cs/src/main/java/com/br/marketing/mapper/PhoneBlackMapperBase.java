package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneBlack;
import com.br.marketing.entity.PhoneBlackExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneBlackMapperBase {
    int countByExample(PhoneBlackExample example);

    int deleteByExample(PhoneBlackExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PhoneBlack record);

    int insertSelective(PhoneBlack record);

    List<PhoneBlack> selectByExample(PhoneBlackExample example);

    PhoneBlack selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PhoneBlack record, @Param("example") PhoneBlackExample example);

    int updateByExample(@Param("record") PhoneBlack record, @Param("example") PhoneBlackExample example);

    int updateByPrimaryKeySelective(PhoneBlack record);

    int updateByPrimaryKey(PhoneBlack record);
}