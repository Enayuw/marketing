package com.br.marketing.mapper;

import com.br.marketing.entity.VariableAllocation;
import com.br.marketing.entity.VariableAllocationExample;
import java.util.List;

import org.apache.ibatis.annotations.Param;
public interface VariableAllocationMapperBase {
    int countByExample(VariableAllocationExample example);

    int deleteByExample(VariableAllocationExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(VariableAllocation record);

    int insertSelective(VariableAllocation record);

    List<VariableAllocation> selectByExampleWithBLOBs(VariableAllocationExample example);

    List<VariableAllocation> selectByExample(VariableAllocationExample example);

    VariableAllocation selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") VariableAllocation record, @Param("example") VariableAllocationExample example);

    int updateByExampleWithBLOBs(@Param("record") VariableAllocation record, @Param("example") VariableAllocationExample example);

    int updateByExample(@Param("record") VariableAllocation record, @Param("example") VariableAllocationExample example);

    int updateByPrimaryKeySelective(VariableAllocation record);

    int updateByPrimaryKeyWithBLOBs(VariableAllocation record);

    int updateByPrimaryKey(VariableAllocation record);

}