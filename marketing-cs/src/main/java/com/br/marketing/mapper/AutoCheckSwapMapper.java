package com.br.marketing.mapper;


import com.br.marketing.vo.autocheck.SenceVO;
import org.apache.ibatis.annotations.Param;


import java.util.List;

public interface AutoCheckSwapMapper extends AutoCheckSwapMapperBase {

    List<SenceVO> searchSenceList(@Param("searchContent") String searchContent);

    /**
     * 根据场景编码列表查询场景信息
     * @param senceCodeList 场景编码列表
     * @return 场景列表
     */
    List<SenceVO> selectBySenceCodes(@Param("senceCodeList") List<String> senceCodeList);
}