package com.br.marketing.mapper;


import com.br.marketing.dto.mark.FlagDataDTO;
import com.br.marketing.entity.FlagData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FlagDataMapper extends FlagDataMapperBase{

    List<FlagDataDTO> queryDataByCellbI_(@Param("querySql") String querySql);

    void insertbI_(@Param("list") List<FlagDataDTO> list);

    int updateTaskIdByLocalId(Long localId);

    int updateByDynamicEncCell(String cellMd5, String cellSha256, String cellLog, String apiCode, String encType);

    int batchUpdateEsStatusById(@Param("ids") List<Long> ids);

    List<FlagData> queryFlagNewCustComputation(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    void batchUpdateFlagNewCustComputationByIds(@Param("ids") List<Long> ids);

    void batchUpdateFlagNewCustComputationByCells(@Param("cells") List<String> cells,
                                                  @Param("flagNewCust") Integer flagNewCust,
                                                  @Param("flagNewCustComputation") Integer flagNewCustComputation);
    List<String> intersectionWithRongshubI_(@Param("cells") List<String> cells);
}