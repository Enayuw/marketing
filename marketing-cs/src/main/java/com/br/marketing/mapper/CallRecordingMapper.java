package com.br.marketing.mapper;

import com.br.marketing.entity.CallRecording;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通话记录表Mapper
 */
public interface CallRecordingMapper extends CallRecordingMapperBase {

    /**
     * 插入所有字段到记录表（动态SQL）
     * @param insertSql 插入SQL语句
     */
    void insertAllFields(@Param("insertSql") String insertSql);

    List<CallRecording> getCallRecord(@Param("apiCode")String apiCode, @Param("date")String date, @Param("indexId")Long indexId,
                                      @Param("pageSize")Integer pageSize);

    int updateBatchByIds(@Param("idList") List<Long> idList, @Param("status") int status);

}