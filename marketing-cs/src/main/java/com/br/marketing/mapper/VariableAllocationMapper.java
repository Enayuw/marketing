package com.br.marketing.mapper;

import java.util.Date;
import java.util.List;

import com.br.marketing.entity.VariableAllocation;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.VariableAllocationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VariableAllocationMapper extends VariableAllocationMapperBase{
    /**
     * 获取配置参数
     * @param apiCode
     * @return
     */
    @AddDataAuth
    VariableAllocation getVariableList(@Param("apiCode")String apiCode, @Param("dataType")String dataType);

    /**
     * 获取配置参数
     * @param apiCode
     * @return
     */
    VariableAllocation getVariable(@Param("apiCode")String apiCode, @Param("dataType")String dataType);

    /**
     * 获取true与false的量级
     * @param releaseTime
     * @return
     */
    VariableAllocationVO getVariableAllocationVOtiflash_(@Param("releaseTime") String releaseTime);

    int updateByPrimaryMutchKeySelective(VariableAllocationVO allocationVO);

}