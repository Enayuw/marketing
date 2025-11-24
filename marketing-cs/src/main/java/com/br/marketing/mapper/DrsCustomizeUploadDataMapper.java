package com.br.marketing.mapper;

import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.entity.DrsCustomizeUploadData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DrsCustomizeUploadDataMapper {

    void createDrsCustomizeUploadDataTable(@Param("tCid") String tCid);

    int insertSelective(DrsCustomizeUploadData record);

    DrsCustomizeUploadData selectById(@Param("tCid") String tCid, @Param("sourceId") String sourceId);

    void updateSyncStatusById(@Param("tCid") String tCid, @Param("sourceId") String sourceId, @Param("syncStatus") int syncStatus);

    void updateSyncStatusByIds(@Param("tCid") String tCid, @Param("sourceIds") List<Long> sourceIds, @Param("syncStatus") int syncStatus);

    void updateFlattenStatusByIds(@Param("tCid") String tCid, @Param("sourceIds") List<Long> sourceIds, @Param("flattenStatus") int flattenStatus);

    List<DrsCustomizeUploadData> getDataOfNeedClean(@Param("tCid") String tCid
            , @Param("apiCodes") List<String> apiCodes
            , @Param("receiveDates") List<String> receiveDates
            , @Param("pageSize") Integer pageSize);

    List<DrsCustomizeUploadData> getDataOfToBeSync(@Param("tCid") String tcId, @Param("apiCodes") List<String> apiCodes, @Param("receiveDates")
            List<String> receiveDates, @Param("pageSize") Integer pageSize, @Param("indexId") Long indexId);


    void updateExtendAndStatusById(@Param("tCid") String tCid, @Param("id") Long id,@Param("syncStatus") int syncStatus, @Param("extend") String
            extend);

    List<DrsCustomizeUploadData> selectByApiCodeAndDate(@Param("tCid") String tCid, @Param("apiCode") String apiCode, @Param("createDate") String createDate);

    /**
     * 查询数据的最大最小id
     *
     * @param dateType  日期类型：history表示<=，today表示=
     * @param dateValue 日期值
     */
    Map<String, Long> getDataIdRange(@Param("tCid") String tCid
            , @Param("apiCodes") List<String> apiCodes
            , @Param("dateType") String dateType
            , @Param("dateValue") String dateValue);

    /**
     * 根据id范围查询数据（用于分批处理）
     *
     * @param pageSize  分页大小
     * @param indexId   起始id
     * @param maxId     最大id
     * @param dateType  日期类型：history表示<=，today表示=
     * @param dateValue 日期值
     */
    List<DrsCustomizeUploadData> getDataByIdRange(@Param("tCid") String tCid
            , @Param("apiCodes") List<String> apiCodes
            , @Param("pageSize") Integer pageSize
            , @Param("indexId") Long indexId
            , @Param("maxId") Long maxId
            , @Param("dateType") String dateType
            , @Param("dateValue") String dateValue);

    List<DrsCustomizeUploadData> getDrsCustomizeUploadDataBySyncStatus(@Param("tCid") String tCid,
                                                                       @Param("syncStatus") Integer syncStatus,
                                                                       @Param("minId") Long minId,
                                                                       @Param("pageSize") Integer pageSize);

    List<BQifuUploadDataOriginal> getQiFuUploadDataOriginalBySerialNo(@Param("tCid") String tCid,
                                                                      @Param("serialNo") String serialNo);

}
