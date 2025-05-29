package com.br.marketing.mapper;

import com.br.marketing.entity.UMengInterfaceLog;
import com.br.marketing.entity.UMengInterfaceLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UMengInterfaceLogMapperBase {
    int countByExample(UMengInterfaceLogExample example);

    int deleteByExample(UMengInterfaceLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UMengInterfaceLog record);

    int insertSelective(UMengInterfaceLog record);

    List<UMengInterfaceLog> selectByExample(UMengInterfaceLogExample example);

    UMengInterfaceLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UMengInterfaceLog record, @Param("example") UMengInterfaceLogExample example);

    int updateByExample(@Param("record") UMengInterfaceLog record, @Param("example") UMengInterfaceLogExample example);

    int updateByPrimaryKeySelective(UMengInterfaceLog record);

    int updateByPrimaryKey(UMengInterfaceLog record);
}