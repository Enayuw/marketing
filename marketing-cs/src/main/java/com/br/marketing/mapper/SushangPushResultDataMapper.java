package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.SushangPushResultData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SushangPushResultDataMapper extends SushangPushResultDataMapperBase {
    void insertBatch(@Param("list") List<SushangPushResultData> resultDataList);

    List<SushangPushResultData> getDealDataByCustNum(@Param("custNums") List<String> custNums, @Param("date") String date);

    /**
     * 苏商通话明细回传
     * @param apiCode apiCode
     * @param requestDate requestDate
     * @param beginId beginId
     * @param endId endId
     * @return java.util.List<com.br.marketing.entity.MarketingTransferSyncUser> 查询到的MarketingTransferSyncUser集合
     */
    List<SushangPushResultData> getTransferByRequestDateSuShang(@Param("apiCode") String apiCode
            , @Param("requestDate") String requestDate
            , @Param("beginId") Long beginId
            , @Param("endId") Long endId);

    /**
     * 获取满足条件的最小id
     * @param apiCode apiCode
     * @param requestDate requestDate
     * @return Long
     */
    Long minId(@Param("apiCode") String apiCode, @Param("requestDate") String requestDate);

    /**
     * 获取满足条件的最大id
     * @param apiCode apiCode
     * @param requestDate requestDate
     * @return Long
     */
    Long maxId(@Param("apiCode") String apiCode, @Param("requestDate") String requestDate);


}
