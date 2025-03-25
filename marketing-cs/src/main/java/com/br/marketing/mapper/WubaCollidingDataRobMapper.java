package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingData;
import com.br.marketing.entity.WubaCollidingDataFront;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface WubaCollidingDataRobMapper extends WubaCollidingDataRobMapperBase {
    void batchSaveData(@Param("robs") List<WubaCollidingDataFront> robs, @Param("apiCode") String apiCode);

    List<WubaCollidingData> selectCollidingData(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode);

    void batchUpdatePushTimeById(@Param("robs") List<WubaCollidingData> robs, @Param("sourceType") String sourceType);

    void batchDeleteByCell(@Param("cells") List<String> cells, @Param("apiCode") String apiCode);

    List<WubaCollidingData> selectHighValueCollidingData(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode,
                                                         @Param("nowDate") Date nowDate, @Param("fileNames") List<String> fileNames);

    void batchSaveTrueToFalseData(@Param("cells") List<String> cells, @Param("apiCode") String apiCode,
                                  @Param("dataSourceType") String dataSourceType);

    List<WubaCollidingData> selectReavedData(@Param("pageSize") Integer pageSize, @Param("apiCode") String apiCode,
                                             @Param("pushTimeEnd") Date pushTimeEnd,
                                             @Param("loopCycleReavedFileId") Long loopCycleReavedFileId);

    void batchSaveReavedDataInToRob(@Param("cells") List<String> cells, @Param("apiCode") String apiCode,
                                    @Param("dataSourceType") String dataSourceType, @Param("packageId") Long packageId);

    List<String> selectDuplicateDataByFileId(@Param("list") List<String> list, @Param("apiCode") String apiCode,
                                             @Param("highValueAndReavedFileIds") String highValueAndReavedFileIds);

    List<String> selectDuplicateDataByCreateTime(@Param("list") List<String> list, @Param("apiCode") String apiCode, @Param("today") Date today,
                                                 @Param("tomorrow") Date tomorrow);
}