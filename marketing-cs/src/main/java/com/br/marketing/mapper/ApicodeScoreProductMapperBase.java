package com.br.marketing.mapper;

import com.br.marketing.entity.ApicodeScoreProduct;
import com.br.marketing.entity.ApicodeScoreProductExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApicodeScoreProductMapperBase {
    int countByExample(ApicodeScoreProductExample example);

    int deleteByExample(ApicodeScoreProductExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ApicodeScoreProduct record);

    int insertSelective(ApicodeScoreProduct record);

    List<ApicodeScoreProduct> selectByExample(ApicodeScoreProductExample example);

    ApicodeScoreProduct selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ApicodeScoreProduct record, @Param("example") ApicodeScoreProductExample example);

    int updateByExample(@Param("record") ApicodeScoreProduct record, @Param("example") ApicodeScoreProductExample example);

    int updateByPrimaryKeySelective(ApicodeScoreProduct record);

    int updateByPrimaryKey(ApicodeScoreProduct record);
}