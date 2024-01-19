package com.br.marketing.mapper;

import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.vo.ConditionOfScoreVO;
import com.br.marketing.vo.ScoreConditionDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScoreSearchConditionMapper extends ScoreSearchConditionMapperBase {

    List<ScoreConditionDetailVO> getScoreListBySearch(SearchConditionDTO dto);

    Integer getScoreCountBySearch(SearchConditionDTO dto);

    List<ConditionOfScoreVO> getScoreByNameNumberList(@Param("ids") List<Long> ids, @Param("searchTxt") String searchTxt);

    List<ConditionOfScoreVO> getScoreByConditionType(@Param("apiCode") String apiCode
            , @Param("conditionType") Integer conditionType,@Param("ruleLabel") String ruleLabel);
}