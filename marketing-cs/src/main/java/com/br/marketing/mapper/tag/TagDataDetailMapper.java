package com.br.marketing.mapper.tag;


import org.apache.ibatis.annotations.Param;

public interface TagDataDetailMapper extends TagDataDetailMapperBase {

    int queryPreviewTotalbI_(@Param("querySql") String querySql);
}