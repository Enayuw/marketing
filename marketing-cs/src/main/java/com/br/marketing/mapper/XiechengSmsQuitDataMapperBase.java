package com.br.marketing.mapper;

import com.br.marketing.entity.XiechengSmsQuitData;
import com.br.marketing.entity.XiechengSmsQuitDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface XiechengSmsQuitDataMapperBase {
    long countByExample(XiechengSmsQuitDataExample example);

    int deleteByExample(XiechengSmsQuitDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XiechengSmsQuitData record);

    int insertSelective(XiechengSmsQuitData record);

    List<XiechengSmsQuitData> selectByExample(XiechengSmsQuitDataExample example);

    XiechengSmsQuitData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XiechengSmsQuitData record, @Param("example") XiechengSmsQuitDataExample example);

    int updateByExample(@Param("record") XiechengSmsQuitData record, @Param("example") XiechengSmsQuitDataExample example);

    int updateByPrimaryKeySelective(XiechengSmsQuitData record);

    int updateByPrimaryKey(XiechengSmsQuitData record);
}