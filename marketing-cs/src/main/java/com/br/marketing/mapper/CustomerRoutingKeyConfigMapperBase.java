package com.br.marketing.mapper;

import com.br.marketing.entity.CustomerRoutingKeyConfig;
import com.br.marketing.entity.CustomerRoutingKeyConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerRoutingKeyConfigMapperBase {
    int countByExample(CustomerRoutingKeyConfigExample example);

    int deleteByExample(CustomerRoutingKeyConfigExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(CustomerRoutingKeyConfig record);

    int insertSelective(CustomerRoutingKeyConfig record);

    List<CustomerRoutingKeyConfig> selectByExampleWithBLOBs(CustomerRoutingKeyConfigExample example);

    List<CustomerRoutingKeyConfig> selectByExample(CustomerRoutingKeyConfigExample example);

    CustomerRoutingKeyConfig selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") CustomerRoutingKeyConfig record, @Param("example") CustomerRoutingKeyConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") CustomerRoutingKeyConfig record, @Param("example") CustomerRoutingKeyConfigExample example);

    int updateByExample(@Param("record") CustomerRoutingKeyConfig record, @Param("example") CustomerRoutingKeyConfigExample example);

    int updateByPrimaryKeySelective(CustomerRoutingKeyConfig record);

    int updateByPrimaryKeyWithBLOBs(CustomerRoutingKeyConfig record);

    int updateByPrimaryKey(CustomerRoutingKeyConfig record);
}