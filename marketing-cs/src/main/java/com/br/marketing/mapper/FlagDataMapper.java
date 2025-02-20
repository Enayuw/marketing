package com.br.marketing.mapper;


import com.br.marketing.dto.mark.FlagDataDTO;
import com.br.marketing.entity.FlagData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FlagDataMapper extends FlagDataMapperBase{

    List<Map<String, Object>> queryDataByCellbI_(@Param("querySql") String querySql);

    void insertbI_(@Param("querySql") String querySql);

    int updateTaskIdByLocalId(Long localId);

    int updateByDynamicEncCell(String cellMd5, String cellSha256, String cellLog, String apiCode, String encType);

    int batchUpdateEsStatusById(@Param("ids") List<Long> ids,@Param("status") Integer status);

    List<FlagData> queryFlagNewCustComputation(@Param("pageSize") Integer pageSize);

    void batchUpdateFlagNewCustComputationByIds(@Param("ids") List<Long> ids);

    void batchUpdateFlagNewCustComputationByCells(@Param("cells") List<String> cells,
                                                  @Param("flagNewCust") Integer flagNewCust,
                                                  @Param("flagNewCustComputation") Integer flagNewCustComputation);
    List<String> intersectionWithRongshubI_(@Param("cells") List<String> cells);
}