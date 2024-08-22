package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTaskUserType;
import com.br.marketing.entity.MarketingTaskUserTypeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTaskUserTypeMapper {
    int countByExample(MarketingTaskUserTypeExample example);

    int deleteByExample(MarketingTaskUserTypeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTaskUserType record);

    int insertSelective(MarketingTaskUserType record);

    List<MarketingTaskUserType> selectByExample(MarketingTaskUserTypeExample example);

    MarketingTaskUserType selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTaskUserType record, @Param("example") MarketingTaskUserTypeExample example);

    int updateByExample(@Param("record") MarketingTaskUserType record, @Param("example") MarketingTaskUserTypeExample example);

    int updateByPrimaryKeySelective(MarketingTaskUserType record);

    int updateByPrimaryKey(MarketingTaskUserType record);

    List<String> queryUserTypeByBatchNumbertikv_(@Param("batchNumber") String batchNumber);
    List<String> queryUserTypeByApiCodetikv_(@Param("apiCode") String apiCode);

    List<String> queryUserTypeByBatchNumberAndApiCodetikv_(@Param("apiCode") String apiCode, @Param("batchNumber") String batchNumber);

}