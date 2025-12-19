package com.br.marketing.mapper;


import com.br.marketing.vo.autocheck.AutoCheckSenceVO;
import org.apache.ibatis.annotations.Param;


import java.util.List;

public interface AutoCheckSenceDictMapper extends AutoCheckSenceDictMapperBase {

    List<AutoCheckSenceVO> searchSenceList(@Param("searchContent") String searchContent);

    /**
     * 根据场景编码列表查询场景信息
     * @param senceCodeList 场景编码列表
     * @return 场景列表
     */
    List<AutoCheckSenceVO> selectBySenceCodes(@Param("senceCodeList") List<String> senceCodeList);
}