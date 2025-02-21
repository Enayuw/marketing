package com.br.marketing.mapper;


import com.br.marketing.dto.mark.FlagDataCarryLogCell;
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

    List<FlagData> queryFlagNewCustComputation(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    List<FlagData> queryFlagBlackListComputation(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    void batchUpdateFlagNewCustComputationByIds(@Param("ids") List<Long> ids);

    void batchUpdateFlagBlackListComputationByIds(@Param("ids") List<Long> ids);

    void batchUpdateFlagNewCustComputationByCells(@Param("cells") List<String> cells,
                                                  @Param("flagNewCust") Integer flagNewCust,
                                                  @Param("flagNewCustComputation") Integer flagNewCustComputation);

    void batchUpdateFlagBlackListComputationByCells(@Param("cells") List<String> cells,
                                                  @Param("flagBlacklist") Integer flagBlacklist,
                                                  @Param("flagBlacklistComputation") Integer flagBlacklistComputation);

    List<String> intersectionWithRongshubI_(@Param("cells") List<String> cells);

    List<String> intersectionWithBlackList(@Param("cells") List<String> cells,@Param("type") Integer type);

    List<FlagDataCarryLogCell> queryLogCellByDatebI_(@Param("apiCode") String apiCode,
                                                     @Param("date") String date,
                                                     @Param("pageSize") Integer pageSize);

    List<FlagData> queryRiskGroupAndInterestData(String apiCode, Integer pageSize);
    int batchUpdateFlagStatusById(@Param("ids") List<Long> ids, Integer flagStatus);

    List<FlagData> queryOdsOrgDataByCellbI_(@Param("cells") List<String> cells);

    int batchUpdateRiskGroupAndInterestFlagById(@Param("data") List<FlagData> data, Integer flagStatus, String flagRiskGroup);
}