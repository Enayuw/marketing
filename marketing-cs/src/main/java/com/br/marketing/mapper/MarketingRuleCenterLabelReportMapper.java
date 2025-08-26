package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName MarketingRuleCenterLabelReportMapper
 * @Author hang.zhou
 * @Date 2025/8/6
 */
@Repository
public interface MarketingRuleCenterLabelReportMapper extends MarketingRuleCenterLabelReportMapperBase{

    List<Long> selectLabelIdWithLabelName(@Param("whereStr") String whereStr, @Param("labelNme") String labelNme);
}
