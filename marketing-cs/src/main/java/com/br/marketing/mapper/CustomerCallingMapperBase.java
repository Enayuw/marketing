package com.br.marketing.mapper;

import com.br.marketing.entity.CustomerCalling;
import com.br.marketing.entity.CustomerCallingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerCallingMapperBase {
    int countByExample(CustomerCallingExample example);

    int deleteByExample(CustomerCallingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CustomerCalling record);

    int insertSelective(CustomerCalling record);

    List<CustomerCalling> selectByExample(CustomerCallingExample example);

    CustomerCalling selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CustomerCalling record, @Param("example") CustomerCallingExample example);

    int updateByExample(@Param("record") CustomerCalling record, @Param("example") CustomerCallingExample example);

    int updateByPrimaryKeySelective(CustomerCalling record);

    int updateByPrimaryKey(CustomerCalling record);
}