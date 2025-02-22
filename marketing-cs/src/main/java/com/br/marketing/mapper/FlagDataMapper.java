package com.br.marketing.mapper;


import com.br.marketing.dto.mark.FlagDataCarryLogCell;
import com.br.marketing.dto.mark.FlagDataDTO;
import com.br.marketing.entity.FlagData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FlagDataMapper extends FlagDataMapperBase {

    List<Map<String, Object>> queryDataByCellbI_(@Param("querySql") String querySql);

    void insertbI_(@Param("querySql") String querySql);

    int updateTaskIdByLocalId(@Param("localId") Long localId, @Param("taskId") Long taskId);

    int updateByDynamicEncCell(@Param("cellMd5") String cellMd5, @Param("cellSha256") String cellSha256, @Param("cellLog") String cellLog, @Param(
            "apiCode") String apiCode, @Param("encType") String encType, @Param("appletDate") String appletDate,
                               @Param("userType") String userType);

    int batchUpdateEsStatusById(@Param("ids") List<Long> ids, @Param("status") Integer status);

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

    List<FlagData> queryRiskGroupAndInterestData(@Param("apiCode") String apiCode, @Param("pageSize") Integer pageSize);

    int batchUpdateFlagStatusById(@Param("ids") List<Long> ids, @Param("flagStatus") Integer flagStatus);

    List<FlagData> queryOdsOrgDataByCellbI_(@Param("cells") List<String> cells);

    int batchUpdateRiskGroupFlagById(@Param("data") List<FlagData> data,
                                                @Param("flagRiskGroup") String flagRiskGroup);

    int batchUpdateInterestFlagById(@Param("data") List<FlagData> data, @Param("flagStatus") Integer flagStatus,
                                                @Param("flagInterest") String flagInterest);

    int batchUpdateHighRiskStatusById(@Param("ids") List<Long> ids, @Param("flagHighRiskComputation") Integer flagHighRiskComputation);

    List<FlagData> queryFlagWhiteListComputation(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    List<FlagData> queryCellListComputation(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    void batchUpdateCellDecodeListByIds(@Param("list") List<FlagData> list,  @Param("flagCellDecodeComputation") Integer flagCellDecodeComputation);

    void batchUpdateFlagWhiteListByIds(@Param("ids") List<Long> ids,
                                                  @Param("flagWhitelistComputation") Integer flagWhitelistComputation,
                                                  @Param("flagWhitelist") Integer flagWhitelist);

    void batchUpdateFlagWhiteListComputationByIds(@Param("ids") List<Long> ids,
                                                  @Param("flagWhitelistComputation") Integer flagWhitelistComputation);
    void batchUpdateCellDecodeListComputationByIds(@Param("ids") List<Long> ids,
                                                  @Param("flagWhitelistComputation") Integer flagWhitelistComputation);
}