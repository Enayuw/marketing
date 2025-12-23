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
     * 查询指定日期（yyyy-MM-dd）的比对结果。
     * <p>
     * compare_time 为 VARCHAR，通常保存为 yyyy-MM-dd HH:mm:ss；此处用前缀匹配当天。
     */
    List<AutoCheckResultLog> selectByCompareTime(@Param("today") String today);
}
