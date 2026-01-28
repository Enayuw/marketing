package com.br.marketing.mapper;

import com.br.marketing.entity.AutoCheckResultLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AutoCheckResultLogMapper extends AutoCheckResultLogMapperBase {

    /**
     * 批量保存
     */
    void batchInsert(@Param("saveList") List<AutoCheckResultLog> saveList);

    /**
     * 查询指定时间区间（yyyy-MM-dd HH:mm:ss）的比对结果。
     * <p>
     * compare_time 为 VARCHAR，通常保存为 yyyy-MM-dd HH:mm:ss，且字典序=时间序，可直接做区间比较。
     * startTime/endTime 传 yyyy-MM-dd HH:mm:ss（包含边界）。
     */
    List<AutoCheckResultLog> selectByCodeListAndTime(@Param("startTime") String startTime,
                                                     @Param("endTime") String endTime,
                                                     @Param("apiCodeList") List<String> apiCodeList,
                                                     @Param("sceneCodeList") List<String> sceneCodeList);
}
