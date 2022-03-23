package com.br.marketing.mapper;

import com.br.marketing.vo.TaskInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface MarketingTaskExtendMapper extends MarketingTaskExtendMapperBase{

    /**
     * 产品集合
     * @param batchNumber
     * @return
     */
    List<TaskInfoVO> getProducts(@Param("batchNumbers") List<String> batchNumbers);
}