package com.br.marketing.mapper;


import com.br.marketing.entity.QifuSaveReachDeleteRecordApiPushLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QifuSaveReachDeleteRecordApiPushLogMapper extends QifuSaveReachDeleteRecordApiPushLogMapperBase {

    /**
     * 2023-09-27 15:13
     * 查询补偿数据集合
     */
    List<QifuSaveReachDeleteRecordApiPushLog> findBatchNoByStatusPage(@Param("apiCode") String apiCode
            , @Param("batchNo") String batchNo
            , @Param("status") int status
            , @Param("pageSize") int pageSize);

    /**
     * 2023-09-27 15:13
     * 获取推送日期中最后的一个批次号
     */
    String getBatchNoByTimeMax(@Param("apiCode") String apiCode
            , @Param("pushDate") String pushDate);

    /**
     * 2023-09-27 15:13
     * 统计接口异常导致推送异常
     */
    List<QifuSaveReachDeleteRecordApiPushLog> getApiErrorStatistics(@Param("apiCode") String apiCode
            , @Param("pushDate") String pushDate);

    /**
     * 2023-09-27 15:13
     * 统计奇富侧业务异常导致推送异常
     */
    List<QifuSaveReachDeleteRecordApiPushLog> getQiFuBizErrorStatistics(@Param("apiCode") String apiCode
            , @Param("pushDate") String pushDate);

    /**
     * 2023-09-27 15:13
     * 统计业务异常导致推送异常
     */
    List<QifuSaveReachDeleteRecordApiPushLog> getBizErrorStatistics(@Param("apiCode") String apiCode
            , @Param("pushDate") String pushDate);

    /**
     * 2023-10-10 11:14
     * 批量更新状态
     */
    int updateStatusByIds(@Param("status") int status, @Param("ids") List<Long> ids);
}