package com.br.marketing.mapper;

import com.br.marketing.entity.PushTransferCustomerLog;
import com.br.marketing.entity.PushTransferCustomerLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PushTransferCustomerLogMapperBase {
    int countByExample(PushTransferCustomerLogExample example);

    int deleteByExample(PushTransferCustomerLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PushTransferCustomerLog record);

    int insertSelective(PushTransferCustomerLog record);

    List<PushTransferCustomerLog> selectByExample(PushTransferCustomerLogExample example);

    PushTransferCustomerLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PushTransferCustomerLog record, @Param("example") PushTransferCustomerLogExample example);

    int updateByExample(@Param("record") PushTransferCustomerLog record, @Param("example") PushTransferCustomerLogExample example);

    int updateByPrimaryKeySelective(PushTransferCustomerLog record);

    int updateByPrimaryKey(PushTransferCustomerLog record);
}