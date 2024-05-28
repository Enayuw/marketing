package com.br.marketing.mapper;

import com.br.marketing.entity.PushCustomerFileInfo;
import com.br.marketing.entity.PushCustomerFileInfoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PushCustomerFileInfoMapperBase {
    int countByExample(PushCustomerFileInfoExample example);

    int deleteByExample(PushCustomerFileInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PushCustomerFileInfo record);

    int insertSelective(PushCustomerFileInfo record);

    List<PushCustomerFileInfo> selectByExample(PushCustomerFileInfoExample example);

    PushCustomerFileInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PushCustomerFileInfo record, @Param("example") PushCustomerFileInfoExample example);

    int updateByExample(@Param("record") PushCustomerFileInfo record, @Param("example") PushCustomerFileInfoExample example);

    int updateByPrimaryKeySelective(PushCustomerFileInfo record);

    int updateByPrimaryKey(PushCustomerFileInfo record);
}