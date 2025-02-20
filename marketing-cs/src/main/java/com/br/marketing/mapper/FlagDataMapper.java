package com.br.marketing.mapper;


import com.br.marketing.dto.mark.FlagDataDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FlagDataMapper extends FlagDataMapperBase{

    List<FlagDataDTO> queryDataByCellbI_(@Param("querySql") String querySql);

    void insertbI_(@Param("list") List<FlagDataDTO> list);

    int updateTaskIdByLocalId(Long localId);

    int updateByDynamicEncCell(String cellMd5, String cellSha256, String cellLog, String apiCode, String encType);

    int batchUpdateEsStatusById(@Param("ids") List<Long> ids);
}