package com.br.marketing.mapper;

import com.br.marketing.entity.CustomerTransferDataReceive;
import com.br.marketing.entity.CustomerTransferDataReceiveExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerTransferDataReceiveMapperBase {
    int countByExample(CustomerTransferDataReceiveExample example);

    int deleteByExample(CustomerTransferDataReceiveExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CustomerTransferDataReceive record);

    int insertSelective(CustomerTransferDataReceive record);

    List<CustomerTransferDataReceive> selectByExample(CustomerTransferDataReceiveExample example);

    CustomerTransferDataReceive selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CustomerTransferDataReceive record, @Param("example") CustomerTransferDataReceiveExample example);

    int updateByExample(@Param("record") CustomerTransferDataReceive record, @Param("example") CustomerTransferDataReceiveExample example);

    int updateByPrimaryKeySelective(CustomerTransferDataReceive record);

    int updateByPrimaryKey(CustomerTransferDataReceive record);
}