package com.br.marketing.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.VariableAllocationVO;
import org.apache.ibatis.annotations.Param;

public interface VariableAllocationMapper extends VariableAllocationMapperBase{
    /**
     * 获取配置参数
     * @param apiCode
     * @return
     */
    @AddDataAuth
    List<VariableAllocationVO> getVariableList(@Param("apiCode")String apiCode, @Param("dataType")String dataType);

    /**
     * 获取true与false的量级
     * @param releaseTime
     * @return
     */
    @AddDataAuth
    BigDecimal getVariableAllocationVO(@Param("releaseTime") Date releaseTime);

    int updateByPrimaryMutchKeySelective(VariableAllocationVO allocationVO);

}