package com.br.marketing.mapper;


import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TagDataDetailMapper extends TagDataDetailMapperBase {

    void refreshbI_(@Param("refreshSql") String refreshSql);

    int queryPreviewTotalbI_(@Param("querySql") String querySql);


    List<String> queryCells(@Param("cells") List<String> cells, @Param("tagCode") String tagCode, @Param("date") String date);
}