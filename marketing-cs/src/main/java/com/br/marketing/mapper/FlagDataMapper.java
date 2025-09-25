package com.br.marketing.mapper;


import com.br.marketing.dto.mark.FlagDataCarryLogCell;
import com.br.marketing.dto.mark.FlagDataEsMark;
import com.br.marketing.entity.FlagData;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface FlagDataMapper extends FlagDataMapperBase {

    List<Map<String, Object>> queryDataByCellbI_(@Param("querySql") String querySql);

    int updateTaskIdByLocalId(@Param("localId") Long localId, @Param("taskId") Long taskId);

    int updateByDynamicEncCell(@Param("cellMd5") String cellMd5, @Param("cellSha256") String cellSha256, @Param("cellLog") String cellLog,
                               @Param("apiCode") String apiCode, @Param("encType") String encType, @Param("appletDate") String appletDate,
                               @Param("userType") String userType);

    int updateWhenDecodeFail(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate,
                             @Param("custNum") String custNum, @Param("extend") String extend);

    int batchUpdateEsStatusById(@Param("ids") List<Long> ids, @Param("status") Integer status);

    List<FlagData> queryFlagNewCustComputation(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    List<FlagData> queryFlagBlackListComputation(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    void batchUpdateFlagNewCustComputationByIds(@Param("ids") List<Long> ids, @Param("tag") Integer tag);

    void batchUpdateFlagBlackListComputationByIds(@Param("ids") List<Long> ids, @Param("tag") Integer tag);

    void batchUpdateFlagNewCustComputationByCells(@Param("cells") List<String> cells,
                                                  @Param("flagNewCust") Integer flagNewCust,
                                                  @Param("flagNewCustComputation") Integer flagNewCustComputation);

    void batchUpdateFlagBlackListComputationByCells(@Param("cells") List<String> cells,
                                                    @Param("flagBlacklist") Integer flagBlacklist,
                                                    @Param("flagBlacklistComputation") Integer flagBlacklistComputation);


    List<String> intersectionWithRongshubI_(@Param("cells") List<String> cells);

    List<String> intersectionWithBlackList(@Param("cells") List<String> cells, @Param("type") Integer type);

    List<FlagDataCarryLogCell> queryLogCellByDate(@Param("apiCode") String apiCode,
                                                  @Param("appletDate") String appletDate,
                                                  @Param("pageSize") Integer pageSize);

    List<FlagData> queryRiskGroupAndInterestData(@Param("apiCode") String apiCode, @Param("pageSize") Integer pageSize);

    int batchUpdateFlagStatusById(@Param("ids") List<Long> ids, @Param("flagStatus") Integer flagStatus);

    List<FlagData> queryOdsOrgDataByCellbI_(@Param("cells") List<String> cells);

    int batchUpdateRiskGroupFlagById(@Param("data") List<FlagData> data,
                                     @Param("flagRiskGroup") String flagRiskGroup);

    int batchUpdateInterestFlagById(@Param("data") List<FlagData> data, @Param("flagStatus") Integer flagStatus,
                                    @Param("flagInterest") String flagInterest);

    int batchUpdateHighRiskStatusByIds(@Param("ids") List<Long> ids,
                                       @Param("flagHighRiskComputation") Integer flagHighRiskComputation,
                                       @Param("flagWhitelistComputation") Integer flagWhitelistComputation);

    List<FlagData> queryFlagWhiteListComputation(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    List<FlagData> queryCellListComputation(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    void batchUpdateCellDecodeListByIds(@Param("flagData") FlagData flagData);

    void batchUpdateFlagWhiteListByIds(@Param("ids") List<Long> ids,
                                       @Param("flagWhitelistComputation") Integer flagWhitelistComputation,
                                       @Param("flagWhitelist") Integer flagWhitelist);

    void batchUpdateFlagWhiteListComputationByIds(@Param("ids") List<Long> ids,
                                                  @Param("flagWhitelistComputation") Integer flagWhitelistComputation);

    void batchUpdateCellDecodeListComputationByIds(@Param("ids") List<Long> ids,
                                                   @Param("flagWhitelistComputation") Integer flagWhitelistComputation);

    List<FlagDataEsMark> queryEsMarkByDate(@Param("apiCode") String apiCode,
                                           @Param("date") String date,
                                           @Param("pageSize") Integer pageSize);

    List<String> queryColumnNamebI_(@Param("tableName") String tableName);

    void insertbI_(@Param("querySql") String querySql);

    Long queryCountBySql(@Param("querySql") String querySql);

    Long queryCountBySqlbI_(@Param("querySql") String querySql);

    @MapKey(value = "column_name")
    List<Map<String, Object>> getTableColumnsbI_(@Param("tableName") String tableName);

    void createTablebI_(@Param("createSql") String createSql);
}